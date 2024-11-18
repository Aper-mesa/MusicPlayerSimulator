package Download;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        // 创建下载管理器
        DownloadManager downloadManager = new DownloadManager();

        // 控制台提示用户输入
        Scanner scanner = new Scanner(System.in);
        String command;
        boolean isRunning = true;

        System.out.println("Welcome to the Download Manager!");
        System.out.println("Available commands:");
        System.out.println("  start <taskId> <sourcePath> <destinationPath> - Start a new download task");
        System.out.println("  pause <taskId> - Pause the download task");
        System.out.println("  resume <taskId> - Resume the download task");
        System.out.println("  cancel <taskId> - Cancel the download task");
        System.out.println("  exit - Exit the program");

        while (isRunning) {
            System.out.print("Enter command: ");
            command = scanner.nextLine();
            String[] parts = command.split(" ");

            if (parts.length == 0) {
                System.out.println("Invalid command. Try again.");
                continue;
            }

            switch (parts[0].toLowerCase()) {
                case "start":
                    if (parts.length != 4) {
                        System.out.println("Usage: start <taskId> <sourcePath> <destinationPath>");
                        break;
                    }
                    String taskId = parts[1];
                    String sourcePath = parts[2];
                    String destinationPath = parts[3];

                    ProgressCallback callback = new ConsoleProgressCallback();
                    downloadManager.startDownload(taskId, sourcePath, destinationPath, callback);
                    System.out.println("Task " + taskId + " started.");
                    break;

                case "pause":
                    if (parts.length != 2) {
                        System.out.println("Usage: pause <taskId>");
                        break;
                    }
                    taskId = parts[1];
                    downloadManager.pauseDownload(taskId);
                    System.out.println("Task " + taskId + " paused.");
                    break;

                case "resume":
                    if (parts.length != 2) {
                        System.out.println("Usage: resume <taskId>");
                        break;
                    }
                    taskId = parts[1];
                    downloadManager.resumeDownload(taskId);
                    System.out.println("Task " + taskId + " resumed.");
                    break;

                case "cancel":
                    if (parts.length != 2) {
                        System.out.println("Usage: cancel <taskId>");
                        break;
                    }
                    taskId = parts[1];
                    downloadManager.cancelDownload(taskId);
                    System.out.println("Task " + taskId + " cancelled.");
                    break;

                case "exit":
                    isRunning = false;
                    System.out.println("Shutting down Download Manager...");
                    downloadManager.shutdown();
                    System.out.println("Goodbye!");
                    break;

                default:
                    System.out.println("Unknown command. Try again.");
            }
        }

        scanner.close();
    }
}
