package org.opentosca.nodetypeimplementations;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebService
public class UbuntuUpdate extends AbstractIAService {
    private static final Logger LOG = LoggerFactory.getLogger(UbuntuUpdate.class);

    public static void main(String[] args) throws IOException {
        UbuntuUpdate test = new UbuntuUpdate();

        String privateKey = "-----BEGIN RSA PRIVATE KEY-----\n" +
                "MIIEpAIBAAKCAQEAjSvRng85AMrtOAcOZ4k0NsswL5kiB2Hya034+IJyN2Ktfbky\n" +
                "+8AwO0oZsvg5gaN5vcNFImHLOU0jfPisWuQNVBWZ5fMDJQgBLOp0OphAJYKKBH63\n" +
                "HekdCPO/C7YySL9xiOEK2c9mxmBZsN6ZkjXECARstwhGexlRY3VC5oF0HCctWIek\n" +
                "Pl6py8FhY+Fdrs7VGFxNoIpMWH55ahm0Dqp838zxL8lvrLiJx3wCw3IiqnpZkOen\n" +
                "vtyQpxGLtP70I/ilXWgUbRwq2IGGznHAvDxuKPPbezFck9HscXz6NgGPmeALtaEu\n" +
                "4aF8KaCYoJOWXJu+yqdB4PJGOGLeCulMkOOUswIDAQABAoIBAE47zWPZrc5ppwLY\n" +
                "rjvkhmIjQmXuaDRNLIoY4PPfXqqg7eJxovZvMIt66jkLZNsaHFp0f7ipF3V+8T7N\n" +
                "lL6eCWDiw7HydXitMfGRUK40N8BR0mjhTFuwF14hrmswIje8mI+xn1DLpnojZTEm\n" +
                "e1bSovuKcDESzfRkOH10u4mAjjcXEDlU8PMC0ELNglnCeuKFUywA4pwvBKrsnn9h\n" +
                "1jS3HSgZVWs+/tNJdoGcs85xDQGKL6mEnuE0rdRRxumWKl8SRMTWNQIglOh9LFUh\n" +
                "Yrk6wU5Y0QpgEfl64AOrPzuhuJBbz18bzOqnmkJXvtA96tTElWUZmlPZElNnKpym\n" +
                "mfqPc+ECgYEAw4AU7TQp4GBB83go45O9l42PTUQdPkS4DJf+BHZuryvQEcG0/tQG\n" +
                "tnbo2pw5TigRQ/22viS7/BsQ1yQA2e9FGOhllY91ZpSbejyhW/o0KCfcRTEPkq3x\n" +
                "JzkkrmCkAtADjmrCF8wJ0ZI/Vsyos+O/kcMLTSdvmPA1I7pPJ9K3wBECgYEAuNus\n" +
                "23tTG7HJ3n4vjWep38O9SR+wvLjcDCCXsE9KufchOd/MxQT8gBVvZzPP2CY/fR8r\n" +
                "Esus4/bMtnzz5W5R0Cw37rfxrCTtIJCBRgOxtlSzz4fDvjqDIfLF2qdzadrWvyYK\n" +
                "gK1Pn2mc7EHbdGNzTEAceJSi2k4uPDVtlaWjjIMCgYEAruoNzxz4yrPUzg0YKGWr\n" +
                "H8mcCTmEII5DTaE/uKZt6XR9uSVi1wzE5MQHGnDkJLVcnUSHGCCtwKZGrvabfGHD\n" +
                "pMFpAiNUmu8jHH7ar/xwZe0j+xQmJspSFRHfa9UzaQDL7cQ+3CPGNSdwXCzIVArU\n" +
                "/4lOtz0tb9ya7thmse6qUMECgYEAhaEPKK3Smw9/PR38o/yO2Rm3KDgHW7VKJF4f\n" +
                "KS14eEwEMcDtVnLPUIuuN21Fzxt/A6TXT0P6m+QZgcV7zZtc+t4sJJ3Fzsn2vHaZ\n" +
                "bJJZEdPTE68xub60coL7sZ8gmCHA4PbP2srt5u/CpyrISC9n9nPV4PiZbLPdSlrY\n" +
                "97eALZ8CgYA7mJcXunS7aw1QVsXeh9shSlWesMqg2LOHbch7ugJDr6VSY7//JJ8w\n" +
                "t3w9eX6LCJawH/A2GjN0hq4s1cHwGUbX1P0NjuueZI/m77vvM6f104Rw3b5SNEOR\n" +
                "cC2++t9rIiSLdnjrCaFwtYAAXIRIDbOPNfgsdlTd931hTAykJvmHgw==\n" +
                "-----END RSA PRIVATE KEY-----";

        test.update("18.192.3.164", privateKey, "ubuntu");
    }

    @WebMethod
    @SOAPBinding
    @Oneway
    public void update(@WebParam(name = "VMIP", targetNamespace = "http://nodetypeimplementations.opentosca.org/") String ip,
                       @WebParam(name = "VMPrivateKey", targetNamespace = "http://nodetypeimplementations.opentosca.org/") String privateKey,
                       @WebParam(name = "VMUserName", targetNamespace = "http://nodetypeimplementations.opentosca.org/") String userName) throws IOException {
        File agentKeyFile = File.createTempFile("agent_ssh", ".key");
        try (BufferedWriter out = new BufferedWriter(new FileWriter(agentKeyFile))) {
            out.write(privateKey);
        } catch (IOException e) {
            LOG.error("Error while writing key file...", e);
            throw e;
        }

        Session agentSession = null;
        try {
            agentSession = createJschSession(agentKeyFile, userName, ip);
            LOG.info("Starting to execute the update...");
            performAgentUpdate(agentSession);
            LOG.info("Update successful!");
        } finally {
            if (agentSession != null) {
                agentSession.disconnect();
            }

            FileUtils.forceDelete(agentKeyFile);
        }

        HashMap<String, String> response = new HashMap<>();
        response.put("Response", "success");

        sendResponse(response);
    }

    private void performAgentUpdate(Session agentSession) {
        this.executeCommand(agentSession, "sudo apt-get update");
    }

    private void executeCommand(Session session, String command) {
        ChannelExec channelExec = null;
        try {
            LOG.info("Executing script: \"{}\"", command);
            channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand(command);

            StringBuilder outputBuffer = new StringBuilder();
            StringBuilder errorBuffer = new StringBuilder();
            InputStream in = channelExec.getInputStream();
            InputStream err = channelExec.getExtInputStream();

            channelExec.connect();
            byte[] tmp = new byte[1024];
            int timer = 0;
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) break;
                    outputBuffer.append(new String(tmp, 0, i));
                }
                while (err.available() > 0) {
                    int i = err.read(tmp, 0, 1024);
                    if (i < 0) break;
                    errorBuffer.append(new String(tmp, 0, i));
                }
                if (channelExec.isClosed() && in.available() == 0 && err.available() == 0) {
                    break;
                }
                if (timer++ % 5 == 0) {
                    LOG.info("Still executing...");
                }
                //noinspection BusyWait
                Thread.sleep(1000);
            }

            if (!errorBuffer.toString().isEmpty()) {
                LOG.error(errorBuffer.toString());
            }
            if (!outputBuffer.toString().isEmpty()) {
                LOG.info(outputBuffer.toString());
            }

            LOG.info("\"{}\" exited with code '{}'", command, channelExec.getExitStatus());
        } catch (JSchException | InterruptedException | IOException e) {
            throw new RuntimeException("Failed to execute command \"" + command + "\"");
        } finally {
            if (channelExec != null) {
                channelExec.disconnect();
            }
        }
    }

    private Session createJschSession(File privateKey, String user, String ip) {
        try {
            JSch jsch = new JSch();
            jsch.addIdentity(privateKey.getAbsolutePath());
            Session session = jsch.getSession(user, ip);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            return session;
        } catch (JSchException e) {
            LOG.error("Failed to connect to machine running on '{}', with username '{}'", ip, user);
            LOG.error("Exception:", e);
            throw new RuntimeException("Failed to connect to machine...");
        }
    }
}
