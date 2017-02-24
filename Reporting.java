import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

import java.sql.SQLException;


public class Reporting {

	public static void Usage(){
		System.out.printf(	"Usage: java Reporting <Username> <Password> <Mode>\n"+
						"\tWhere mode is:\n"+
							"\t\t1- Report Patients Basic Information\n"+
							"\t\t2- Report Doctors Basic Information\n"+
							"\t\t3- Report Admissions Information\n"+
							"\t\t4- Update Admissions Payment\n"+
					"But before you compile or run, you must set up various environment variables\n"+
						"\t> source /cs/bin/oracle-setup\n"+
						"\t> export CLASSPATH=./:/usr/local/oracle11gr203/product/11.2.0/db_1/jdbc/lib/ojdbc6.jar\n"
					);
	}


	public static void mode1(Connection connection){
		System.out.printf("Enter Patient SSN:");
		String ssn = System.console().readLine();
		String q = "SELECT * FROM Patient WHERE SSN =" + ssn + "";
		try {
		Statement stmt = connection.createStatement();
		ResultSet res = stmt.executeQuery(q);
			if(res.next()){
				System.out.printf(
					"Patient SSN:%s\n" +
					"Patient First Name:%s\n"+
					"Patient Last Name:%s\n"+
					"Patient Address:%s\n",
					res.getString("SSN"),
					res.getString("First_Name"),
					res.getString("Last_Name"),
					res.getString("Address"));
			} else System.out.printf("No results\n");
			stmt.close(); res.close();
		} catch (SQLException e) { System.out.printf("error:%s\n", e.getMessage());}
	}

	public static void mode2(Connection connection){
		System.out.printf("Enter Doctor ID:");
		String id = System.console().readLine();
		String q = "select Doctors.ID as ID, First_Name, Last_Name, Gender  FROM (Doctors INNER JOIN Employees ON Doctors.ID = Employees.ID) WHERE Doctors.ID =\'" + id + "\'";
//		System.out.printf("Query:%s\n", q);
		try {
		Statement stmt = connection.createStatement();
		ResultSet res = stmt.executeQuery(q);
			if(res.next()){
				System.out.printf(
					"Doctor ID:%s\n" +
					"Doctor First Name:%s\n"+
					"Doctor Last Name:%s\n"+
					"Doctor Gender:%s\n",
					res.getString("ID"),
					res.getString("First_Name"),
					res.getString("Last_Name"),
					res.getString("Gender"));
			} else System.out.printf("No results\n");
			stmt.close(); res.close();
		} catch (SQLException e) { System.out.printf("error:%s\n", e.getMessage());}
	}

	public static void mode3(Connection connection){
		System.out.printf("Enter Admission Number (ID):");
		String id = System.console().readLine();
		//get misc info
		String q = "select ID, Patiendid, STARTDATE, TotalPayment from admissions where ID = \'" + id + "\'";
		try {
		Statement stmt = connection.createStatement();
		ResultSet res = stmt.executeQuery(q);
			if(res.next()){
				System.out.printf(
					"Admission Number (ID):%s\n" +
					"Patient SSN:%s\n"+
					"Admission date (start date):%s\n"+
					"Total Payment:%s\n",
					res.getString("ID"),
					res.getString("Patiendid"),
					res.getString("StartDate"),
					res.getString("TotalPayment"));
			} else { System.out.printf("No results\n"); return; }
			stmt.close(); res.close();
		} catch (SQLException e) { System.out.printf("error:%s\n", e.getMessage());}


		//get room info (stays)
		System.out.printf("Rooms:\n");
		q = "select ROOM, ENTRY, EXIT from stays where Admission = \'" + id + "\'";
		try {
		Statement stmt = connection.createStatement();
		ResultSet res = stmt.executeQuery(q);
			int i;
			for(i = 0; res.next(); i++){
				System.out.printf(
					"\tRoomNum:%s\t" +
					"FromDate:%s\t"+
					"ToDate:%s\n",
					res.getString("ROOM"),
					res.getString("ENTRY"),
					res.getString("EXIT"));
			} if(i == 0)System.out.printf("No results\n");
			stmt.close(); res.close();
		} catch (SQLException e) { System.out.printf("error:%s\n", e.getMessage());}


		//get examinations info
		//TODO might have an issue for multiple examinations by the same doctor.... should be put a group by on it?
		System.out.printf("Doctors examined the patient in this admission:\n");
		q = " select DOCTOR from examinations where ADMISSION = \'" + id + "\'";
		try {
		Statement stmt = connection.createStatement();
		ResultSet res = stmt.executeQuery(q);
			int i;
			for(i = 0; res.next(); i++){
				System.out.printf(
					"\tDoctor ID:%s\n",
					res.getString("DOCTOR"));
			} if(i == 0)System.out.printf("No results\n");
			stmt.close(); res.close();
		} catch (SQLException e) { System.out.printf("error:%s\n", e.getMessage());}
	}
	public static void mode4(Connection connection){
		System.out.printf("Enter Admission Number (ID):");
		String id = System.console().readLine();
		System.out.printf("Enter the new total payment:");
		String pay = System.console().readLine();
		String q = "update Admissions set TotalPayment=\'" + pay + "\' where id=\'" + id + "\'";
//		System.out.printf("Query:%s\n", q);
		try {
		Statement stmt = connection.createStatement();
		stmt.executeUpdate(q);
		stmt.close();
		} catch (SQLException e) { System.out.printf("error:%s\n", e.getMessage()); return;}
		System.out.printf("Success!\n");
	}

    public static void main(String[] argv) {
	if(argv.length < 3){
		System.out.printf("Not enough arguments!\n");
		Usage();
		return;
	}
	char mode = argv[2].length()==1?argv[2].charAt(0) : 'd';
	switch(mode){
		case '1':
		case '2':
		case '3':
		case '4': break;
		default: System.out.printf("Invalid mode %s\n", argv[2]); Usage(); return;
	}
        System.out.println("-------- Registering Oracle Driver ------");
        try {
		Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
		Usage();
            return;
        }

	System.out.println("-------- Building a Connection ------");
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(
                                                     "jdbc:oracle:thin:@oracle.wpi.edu:1521:orcl", argv[0],
                                                     argv[1]);

        } catch (SQLException e) {
            System.out.println("Connection Failed! Check output console");
		Usage();
            return;
        }
	if(connection == null){
		System.out.println("Failed to make connection!");
		Usage();
		return;
	}
	System.out.printf("Connected\n");
	//can i just have an array of function pointers? Would be nice
	switch(mode){
		case '1': mode1(connection); break;
		case '2': mode2(connection); break;
		case '3': mode3(connection); break;
		case '4': mode4(connection); break;
		default: System.out.printf("Invalid mode %s\n", argv[2]); Usage(); return;
	}

	try {
		connection.close();
	} catch (SQLException e) { System.out.printf("error:%s\n", e.getMessage());}
    }

}
