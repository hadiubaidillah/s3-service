package com.hadiubaidillah.s3.component.scheduler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


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
    @Scheduled(cron = "0 0 1 * * ?")
    // run every 10 seconds
    //@Scheduled(cron = "*/10 * * * * *")
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
                "-h", getDatabaseHost(),
                "-u", dbUser,
                "-p" + dbPassword,
                dbName
        );

        processBuilder.redirectOutput(new File(filePath));
        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                // Convert the .sql file to .zip
                zipSqlFile(filePath);

                // Optionally, delete the original .sql file after zipping
                //Files.deleteIfExists(sqlFilePath);
                System.out.println("Backup completed for database: " + dbName);
            } else {
                System.err.println("Error occurred while backing up database: " + dbName);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String getDatabaseHost() {
        try {
            // Remove "jdbc:mysql://" prefix
            String cleanUrl = dbUrl.replace("jdbc:mysql://", "");

            // Split the URL by '/' to separate the host part
            String[] parts = cleanUrl.split("/");

            // Further split by ':' to separate the port (if any)
            return parts[0].split(":")[0];
        } catch (Exception e) {
            // Handle any errors in URL format
            System.out.println("Invalid JDBC URL format");
            return null;
        }
    }

    private void zipSqlFile(String filePath) throws IOException {

        // Path to the .sql file
        Path sqlFilePath = Paths.get(filePath);

        // Define the output path for the .zip file
        Path zipFilePath = Paths.get(sqlFilePath + ".zip");

        // Create the ZipOutputStream with the .zip output path
        try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(zipFilePath));
             InputStream sqlFileIn = Files.newInputStream(sqlFilePath)) {

            // Create a new zip entry for the SQL file
            ZipEntry zipEntry = new ZipEntry(sqlFilePath.getFileName().toString());
            zipOut.putNextEntry(zipEntry);

            // Read the SQL file and write to the zip entry
            sqlFileIn.transferTo(zipOut);

            // Close the zip entry
            zipOut.closeEntry();
        }

        System.out.println("SQL file compressed to: " + zipFilePath);
    }

}
