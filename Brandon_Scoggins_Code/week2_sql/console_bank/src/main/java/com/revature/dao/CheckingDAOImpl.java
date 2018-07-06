package com.revature.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.revature.models.CheckingAccount;
import com.revature.models.User;
import com.revature.util.ConnectionFactory;

public class CheckingDAOImpl implements CheckingAccountDAO{

	AccountsDAO accountsDAO = new AccountsDAOImpl();
	TransactionDAO transactionDAO = new TransactionDAOImpl();

	@Override
	public double getCheckingBalanceByUser(User currentUser) {

		int userCheckingId = accountsDAO.getUserCheckingId(currentUser.getUserId());

		try(Connection conn = ConnectionFactory.getInstance().getConnection();){

			String sql = "SELECT balance FROM checkingAccounts WHERE accountId = ?";

			PreparedStatement pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, userCheckingId);

			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				return rs.getDouble(1);
			}else {
				System.out.println("unable to get checking balance");
				return 0.0;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public double getCheckingBalanceByAccountId(int accountId) {

		try(Connection conn = ConnectionFactory.getInstance().getConnection();){

			String sql = "SELECT balance FROM checkingAccounts WHERE accountId = ("
					+ "SELECT checkingId FROM accounts WHERE accountId = ?)";

			PreparedStatement pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, accountId);

			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				return rs.getDouble(1);
			}else {
				System.out.println("unable to get checking balance");
				return 0.0;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public boolean depositChecking(User currentUser, int targetAccountId, double amount) {

		double newAmount = 0.0;

		newAmount = getCheckingBalanceByAccountId(targetAccountId) + amount;

		try(Connection conn = ConnectionFactory.getInstance().getConnection();){

			String sql = "UPDATE checkingAccounts SET balance = ? WHERE accountId = ("
					+ "Select checkingId FROM accounts WHERE accountId = ?)";

			PreparedStatement pstmt = conn.prepareStatement(sql);

			pstmt.setDouble(1, newAmount);
			pstmt.setInt(2, targetAccountId);
			System.out.println("\nDepositing $" + amount + " into checking account " + targetAccountId);
			if(pstmt.executeUpdate() != 0) {
				return true;
			}else {
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean withdrawChecking(User currentUser, int targetAccountId, double amount) {

		ArrayList<User> authorizedUsers = accountsDAO.getAllAccountUsers(targetAccountId);

		boolean userAuthorized = false;

		for (User authorizedUser : authorizedUsers) {
			if(authorizedUser.getUserId() == currentUser.getUserId()) {
				userAuthorized = true;
				break;
			}
		}

		double newAmount = 0.0;

		if(userAuthorized) {

			newAmount = getCheckingBalanceByAccountId(targetAccountId) - amount;

			try(Connection conn = ConnectionFactory.getInstance().getConnection();){

				String sql = "UPDATE checkingAccounts SET balance = ? WHERE accountId = ("
						+ "Select checkingId FROM accounts WHERE accountId = ?)";

				PreparedStatement pstmt = conn.prepareStatement(sql);

				pstmt.setDouble(1, newAmount);
				pstmt.setInt(2, targetAccountId);
				System.out.println("\nWithdrawing $" + amount + " from checking account " + targetAccountId);
				if(pstmt.executeUpdate() != 0) {
					return true;
				}else {
					return false;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	@Override		
	public boolean createChecking(User currentUser) {

		try(Connection conn = ConnectionFactory.getInstance().getConnection();){

			String sql = "SELECT checkingId FROM accounts WHERE accountHolderId = ?";

			PreparedStatement pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, currentUser.getUserId());

			ResultSet rs = pstmt.executeQuery();

			if(rs.next()) {
				rs.getInt(1);
				if(!rs.wasNull()) {
					System.out.println("User " + currentUser.getUserId() + " already has a checking account");
					return false;
				}
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		boolean success = false;

		System.out.println("Creating a checking account for " + currentUser.getUserName() + "...");

		try(Connection conn = ConnectionFactory.getInstance().getConnection();){

			String sql = "INSERT INTO checkingAccounts (accountId) VALUES (NULL)";

			Statement stmt = conn.createStatement();

			success = !stmt.execute(sql);

		} catch (SQLException e) {
			e.printStackTrace();
		}

		if(success) {
			System.out.println("Checking account created for " + currentUser.getUserName() + "!\n");
			pairCheckingToAccount(currentUser);
			return true;
		}else {
			System.out.println("Failed to create checking account for " + currentUser.getUserName() + ".\n");
			return false;
		}
	}

	@Override
	public boolean deleteChecking(User currentUser) {

		int checkingId = accountsDAO.getUserCheckingId(currentUser.getUserId());
		System.out.println("got checking id. Attempting to delete..");
		try(Connection conn = ConnectionFactory.getInstance().getConnection();){
			System.out.println("before delete");
			String sql = "DELETE FROM checkingAccounts WHERE accountId = ?";
			System.out.println("after delete");
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, checkingId);

			if (!pstmt.execute()) {
				System.out.println("Checking account successfully deleted.");
				return true;
			}else {
				System.out.println("Checking account not deleted.");
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("after if statement");
		return false;		
	}

	@Override
	public ArrayList<CheckingAccount> getAllCheckingAccounts() {

		ArrayList<CheckingAccount> allCheckingAccounts = new ArrayList<>();
		System.out.println("getting all checking accounts");
		try(Connection conn = ConnectionFactory.getInstance().getConnection();){

			String sql = "SELECT * FROM checkingAccounts ORDER BY accountId";

			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);

			while(rs.next()) {
				allCheckingAccounts.add(new CheckingAccount(rs.getInt(1), rs.getInt(2)));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("retrieved all checking accounts");
		return allCheckingAccounts;
	}

	@Override
	public boolean pairCheckingToAccount(User currentUser) {

		int newestCheckingId = 0;

		try(Connection conn = ConnectionFactory.getInstance().getConnection();){

			String sql = "SELECT MAX(accountId) FROM checkingAccounts";

			Statement stmt = conn.createStatement();

			ResultSet rs = stmt.executeQuery(sql);

			rs.next();
			newestCheckingId = rs.getInt(1);

		} catch (SQLException e) {
			e.printStackTrace();
		}

		try(Connection conn = ConnectionFactory.getInstance().getConnection();){

			String sql = "UPDATE accounts SET checkingId = ? WHERE accountHolderId = ?";

			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, newestCheckingId);
			pstmt.setInt(2, currentUser.getUserId());

			if(pstmt.executeUpdate() != 0) {
				return true;
			}else {
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return true;
	}
}