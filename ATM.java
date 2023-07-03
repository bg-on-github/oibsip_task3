import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.Random;
class Withdraw
{
	//class to be used for withdrawing from account
	public static void with(Connection c, Statement stmt, float amt, int acc_no)
	{
		try
	    {
			stmt.executeUpdate("update account set net_bal = net_bal - "+amt+" where acc_no = "+acc_no+";");
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy      HH:mm:ss");
			Date date = new Date();
			stmt.executeUpdate("insert into transactions(datetime, acc_no, transac_type, amt_trans, trans_to, dep_by) values('"+formatter.format(date)+"', "+acc_no+", 'Withdrawal', "+amt+", "+acc_no+", "+acc_no+");");
			c.commit();
			System.out.println("Withdrawn Rs. "+amt+" from your account successfully.");
		}
		catch (Exception e)
		{
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
	    }
	}
}
class Deposit
{
	//class to be used for depositing money to account
	public static void dep(Connection c, Statement stmt, float amt, int acc_no)
	{
		try
	    {
			String sql = "update account set net_bal = net_bal + "+amt+" where acc_no = "+acc_no+";";
			stmt.executeUpdate(sql);
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy    HH:mm:ss");
			Date date = new Date();
			stmt.executeUpdate("insert into transactions(datetime, acc_no, transac_type, amt_trans, trans_to, dep_by) values('"+formatter.format(date)+"', "+acc_no+", 'Deposit', "+amt+", "+acc_no+", "+acc_no+");");
			c.commit();
			System.out.println("Deposited Rs. "+amt+" into your account successfully.");
		}
	    catch (Exception e)
	    {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
	    }
	}
}
class Transfer
{
	//class to be used for transfer amounts between accounts
	public static void tran(Connection c, Statement stmt, float amt, int acc_no_from, int acc_no_to)
	{
		try
		{
			stmt.executeUpdate("update account set net_bal = net_bal - "+amt+" where acc_no = "+acc_no_from+";");
			stmt.executeUpdate("update account set net_bal = net_bal + "+amt+" where acc_no = "+acc_no_to+";");
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy    HH:mm:ss");
			Date date = new Date();
			stmt.executeUpdate("insert into transactions(datetime, acc_no, transac_type, amt_trans, trans_to, dep_by) values('"+formatter.format(date)+"', "+acc_no_from+", 'Transferral', "+amt+", "+acc_no_to+", "+acc_no_from+");");
			stmt.executeUpdate("insert into transactions(datetime, acc_no, transac_type, amt_trans, trans_to, dep_by) values('"+formatter.format(date)+"', "+acc_no_to+", 'Transferral', "+amt+", "+acc_no_to+", "+acc_no_from+");");
			c.commit();
			System.out.println("Transferred Rs."+amt+" from "+acc_no_from+"'s account to "+acc_no_to+"'s account successfully.");
		}
		catch (Exception e)
		{
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
	}
}
class History
{
	//class to be used for viewing transaction history of a particular account held by a particular user
	public static void showHistory(Connection c, Statement stmt, int accno)
	{
		//NOTE: The "Transferred to account" and the "Deposited by account" columns will store the account number of the same account when the user is directly withdrawing money from or depositing money to their own account. The appropriate transaction name will be provided in the "Transaction type" column.
		try
	    {
			ResultSet res = stmt.executeQuery("select * from transactions where acc_no = "+accno+";");
			while(res.next())
			{
				System.out.println("Date: "+res.getString("datetime")+"\n Transaction Type: "+res.getString("transac_type")+"\n Amount transacted: Rs. "+res.getFloat("amt_trans")+"\n Transferred to account: "+res.getInt("trans_to")+"\n Deposited by account: "+res.getInt("dep_by"));
				System.out.println("\n-----\n");
			}
			res.close();
		}
		catch (Exception e)
		{
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
	}
}
public class ATM
{
	//driver class
	//following are some auxillary functions
	static boolean isuser(Connection c, Statement stmt, int uid)
	{
		try
	    {
			ResultSet rs = stmt.executeQuery("select userid from user;");
			while(rs.next())
			{
				if(uid == rs.getInt("userid"))
					return true;
			}
			rs.close();
		}
		catch (Exception e)
		{
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
		return false;
	}
	static boolean isuserpwd(Connection c, Statement stmt, int u, String p)
	{
		try
	    {
			ResultSet rs = stmt.executeQuery("select userid, pwd from user;");
			while(rs.next())
			{
				if(u == rs.getInt("userid") && p.equals(rs.getString("pwd")))
					return true;
			}
			rs.close();
		}
		catch (Exception e)
		{
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
		return false;
	}
	static boolean isuseracc(Connection c, Statement stmt, int u, int newac)
	{
		try
	    {
			ResultSet rs = stmt.executeQuery("select acc_no, userid from account;");
			while(rs.next())
			{
				if(u == rs.getInt("userid") && newac == rs.getInt("acc_no"))
					return true;
			}
			rs.close();
		}
		catch (Exception e)
		{
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
		return false;
	}
	public static void main(String args[])
	{
		float a = 0;
		String p;
		Random r = new Random();
		int newac = 0, newac2 = 0, u=0, ch = 0, choice = 0;
		Scanner sc = new Scanner(System.in);
		Connection c = null;
        Statement stmt = null;
		try
	    {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:atm.db");
			c.setAutoCommit(false);
			stmt = c.createStatement();
			try
			{
				stmt.executeUpdate("create table user (userid int primary key not null, username text not null, pwd text not null);");
				stmt.executeUpdate("create table account (acc_no int primary key not null, userid int not null, net_bal real not null);");
				stmt.executeUpdate("create table transactions (datetime text not null, acc_no int not null, transac_type text not null, amt_trans real not null, trans_to int not null, dep_by int not null);");
				c.commit();
			}
			catch(SQLException e)
			{	}
			System.out.println("===================ATM INTERFACE===================");
			System.out.println("Hello and welcome to our ATM service!\n");
			while(choice!=4)
			{
				//user menu
				System.out.println("------------------MAIN MENU------------------");
				System.out.println("1. Create a user.");
				System.out.println("2. Create an account.");
				System.out.println("3. Login to your account.");
				System.out.println("4. Exit interface.\n");
				System.out.println("Please enter the serial number of the\naction you would like to take:");
				choice = sc.nextInt();
				switch(choice)
				{
					case 1: //new user creation
						u = r.nextInt(9000)+1000;
						sc.nextLine();
						System.out.println("Your user ID is "+u+".\nEnter your name:");
						String un = sc.nextLine();
						System.out.println("Enter a password:");
						p = sc.nextLine();
						stmt.executeUpdate("insert into user (userid, username, pwd) values ("+u+", '"+un+"','"+p+"');");
						c.commit();
						System.out.println(un+"'s user "+u+" has been created!\n\n");
					break;
					case 2://new account creation
					//NOTE: An assumption has been made that than one user may hold more than one account, but vice versa is not a possibility.
						System.out.println("Enter your valid user ID (4 digits):");
						u = sc.nextInt();
						if(isuser(c, stmt, u)==true)
						{
							newac = r.nextInt(90000)+10000;
							stmt.executeUpdate("insert into account values ("+newac+", "+u+", 0.0);");
							c.commit();
							System.out.println("Your account ID is "+newac+".\nNet balance on this account has been set to zero.\n\n");
						}
						else
							System.out.println("This is an invalid ID. Please try again.\n\n");
					break;
					case 3://logging in to a certain user's certain account
						System.out.println("Enter your valid user ID (4 digits):");
						u = sc.nextInt();
						if (isuser(c, stmt, u)==false)
						{
							System.out.println("This is an invalid user ID. Please try again.\n\n");
							continue;
						}
						sc.nextLine();
						System.out.println("Enter your password:");
						p = sc.nextLine();
						if (isuserpwd(c, stmt, u, p) == false)
						{
							System.out.println("This is an invalid password. Please try again.\n\n");
							continue;
						}
						System.out.println("Enter your valid account ID (5 digits):");
						newac = sc.nextInt();
						if (isuseracc(c, stmt, u, newac) == false)
						{
							System.out.println("This is an invalid account. Please try again.\n\n");
							continue;
						}
						System.out.println("Logged in successfully!\nWelcome, "+stmt.executeQuery("select username from user where userid = "+ u +";").getString("username")+"!");
						ch = 0;
						while(ch!=5)
						{
							//account menu
							System.out.println("\n-----------ACCOUNT MENU-----------");
							System.out.println("1. Deposit money to account.\n2. Withdraw money from account.\n3. Transfer money to another account.\n4. See transaction history on this account.\n5. Quit account menu.");
							System.out.println("Please enter the serial number of the\naction you would like to take:");
							ch = sc.nextInt();
							switch(ch)
							{
								case 1://Deposit
									System.out.println("How much to be deposited?\nEnter amount:");
									a = sc.nextFloat();
									Deposit.dep(c, stmt, a, newac);
								break;
								case 2://Withdraw
									System.out.println("How much to be withdrawn?\nEnter amount:");
									a = sc.nextFloat();
									if (stmt.executeQuery("select net_bal from account where acc_no="+newac+";").getFloat("net_bal") > a)
										Withdraw.with(c, stmt, a, newac);
									else
										System.out.println("Not enough money in account, please deposit first!");
								break;
								case 3://Transfer
									System.out.println("Which account to be transferred to?\nEnter account number:");
									newac2 = sc.nextInt();
									System.out.println("How much to be transferred?\nEnter amount:");
									a = sc.nextFloat();
									if (stmt.executeQuery("select net_bal from account where acc_no="+newac+";").getFloat("net_bal") > a)
										Transfer.tran(c, stmt, a, newac, newac2);
									else
										System.out.println("Not enough money in account, please deposit first!");
								break;
								case 4://Show history
									System.out.println("Showing transaction history:");
									History.showHistory(c, stmt, newac);
								break;
								case 5://Quit
									System.out.println("Logging out and redirecting to the main menu...\n");
								break;
								default:
									System.out.println("Invalid!\n");
								break;
							}
						}
					break;
					case 4:
						System.out.println("=====================FAREWELL=====================");
					break;
					default:
						System.out.println("Invalid!\n");
					break;
				}
			}
			stmt.close();
			c.close();
		}
		catch (Exception e)
		{
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
	}
}