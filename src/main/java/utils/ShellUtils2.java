package utils;

import android.os.Handler;
import android.text.TextUtils;

import com.gang.accessibility.R;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by gang on 16-5-10.
 */
public class ShellUtils2 {

    public static final String COMMAND_SU = "su";
    public static final String COMMAND_SH = "sh";
    public static final String COMMAND_EXIT = "exit\n";
    public static final String COMMAND_LINE_END = "\n";

    public static CommandResult execCommand(String[] commands, boolean isRoot, boolean isNeedResultMsg) {
        int result = -1;
        if (commands == null || commands.length == 0) {
            return new CommandResult(result, null, null);
        }

        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = null;
        StringBuilder errorMsg = null;

        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec(isRoot ? COMMAND_SU : COMMAND_SH);
            os = new DataOutputStream(process.getOutputStream());
            for (String command : commands) {
                if (command == null) {
                    continue;
                }

                // donnot use os.writeBytes(commmand), avoid chinese charset error
                os.write(command.getBytes());
                os.writeBytes(COMMAND_LINE_END);
                os.flush();
            }
            os.writeBytes(COMMAND_EXIT);
            os.flush();

            result = process.waitFor();
            // get command result
            if (isNeedResultMsg) {
                successMsg = new StringBuilder();
                errorMsg = new StringBuilder();
                successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
                errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String s;
                while ((s = successResult.readLine()) != null) {
                    successMsg.append(s);
                    successMsg.append("\n");
                }
                while ((s = errorResult.readLine()) != null) {
                    errorMsg.append(s);
                    errorMsg.append("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (process != null) {
                process.destroy();
            }
        }
        return new CommandResult(result, successMsg == null ? null : successMsg.toString(), errorMsg == null ? null
                : errorMsg.toString());
    }

    public static String execCommand(String path, String command, boolean su) throws IOException {
        // start the ls command running
        String[] args;
        if (TextUtils.isEmpty(path)) {
            args = new String[]{su ? "su" : "sh", "-c", command};
        } else {
            args = new String[]{path, command};
        }
        Runtime runtime = Runtime.getRuntime();
        Process proc = runtime.exec(args);

        InputStream inputstream = proc.getInputStream();

        String sb = readStream(inputstream);
        String erro = "null";
        String erroMsg = "null";

        try {
            if (proc.waitFor() != 0) {
                erro = readStream(proc.getErrorStream());
            }
        } catch (InterruptedException e) {
            erroMsg = e.getMessage();
        }

        return "out:" + sb + ",error:" + erro + ",errorMsg：" + erroMsg;
    }

    private static String readStream(InputStream stream) {
        InputStreamReader inputstreamreader = new InputStreamReader(stream);
        BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
        // read the ls output
        String line = "";
        StringBuilder sb = new StringBuilder(line);
        try {
            while ((line = bufferedreader.readLine()) != null) {
                //System.out.println(line);
                sb.append(line);
                sb.append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static class CommandResult {

        public int result;
        public String successMsg;
        public String errorMsg;

        public CommandResult(int result, String successMsg, String errorMsg) {
            this.result = result;
            this.successMsg = successMsg;
            this.errorMsg = errorMsg;
        }

        @Override
        public String toString() {
            return "value:" + result + "\n" + successMsg + "\n" + errorMsg + "\n";
        }
    }

    public static class ShellTask extends Thread {

        private Handler handler;

        private String[] commands;
        private String path;
        private String commandStr;
        private boolean su;

        private int method;

        public ShellTask(Handler handler, String[] commands, boolean su) {
            this.handler = handler;
            this.commands = commands;
            this.method = 1;
            this.su = su;
        }

        public ShellTask(Handler handler, String path, String command, boolean su) {
            this.handler = handler;
            this.path = path;
            this.commandStr = command;
            this.method = 2;
            this.su = su;
        }

        @Override
        public void run() {
            super.run();
            String res = null;
            switch (method) {
                case 1: {
                    ShellUtils2.CommandResult result = ShellUtils2.execCommand(commands, su, true);
                    res = result.toString();
                    break;
                }
                case 2: {
                    try {
                        res = ShellUtils2.execCommand(path, commandStr, su);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
            handler.sendMessage(handler.obtainMessage(R.id.shell_complete, res));
        }
    }
}
