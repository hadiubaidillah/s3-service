package com.hadiubaidillah.s3.component.scheduler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

@Component
public class MySQLDumpScheduler {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${mysqldump.path}")
    private String mysqldumpPath;  // Path to mysqldump binary

    @Value("${backup.directory}")
    private String backupDir;  // Directory to store the backups

    // Run every day at 1 a.m.
    //@Scheduled(cron = "0 0 1 * * ?")
    // run every 10 seconds
    @Scheduled(cron = "*/10 * * * * *")
    public void dumpDatabases() {
        System.out.println("dumpDatabases mau dijalankan");
        try{
            // Load MySQL driver explicitly (if required)
            Class.forName("com.mysql.cj.jdbc.Driver");

            try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SHOW DATABASES")) {

                while (rs.next()) {
                    String dbName = rs.getString(1);

                    // Exclude system databases
                    if (dbName.equalsIgnoreCase("mysql")
                            || dbName.equalsIgnoreCase("information_schema")
                            || dbName.equalsIgnoreCase("performance_schema")
                            || dbName.equalsIgnoreCase("sys")
                            || dbName.equalsIgnoreCase("phpmyadmin")
                            || dbName.equalsIgnoreCase("itjenlhkdb")
                            || dbName.equalsIgnoreCase("itjenlhkdb-report")) {
                        continue;
                    }

                    dumpDatabase(dbName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dumpDatabase(String dbName) {
        System.out.println("dumpDatabase mau dijalankan '"+dbName+"'");
        String filePath = backupDir + File.separator + dbName + ".sql";
        ProcessBuilder processBuilder = new ProcessBuilder(
                mysqldumpPath,
                "-h", "157.66.55.139",
                "-u", dbUser,
                "-p" + dbPassword,
                dbName
        );

        processBuilder.redirectOutput(new File(filePath));
        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Backup completed for database: " + dbName);
            } else {
                System.err.println("Error occurred while backing up database: " + dbName);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
