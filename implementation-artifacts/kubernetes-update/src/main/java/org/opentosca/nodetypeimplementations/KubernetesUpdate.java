package org.opentosca.nodetypeimplementations;

import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import io.kubernetes.client.Exec;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import io.kubernetes.client.util.Streams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebService
public class KubernetesUpdate extends AbstractIAService {
    private static final Logger LOG = LoggerFactory.getLogger(KubernetesUpdate.class);

    public static void main(String[] args) throws IOException {
        Path configFile = Paths.get("C:\\Users\\rosso\\.kube\\config");
        byte[] configContents;
        try {
            configContents = Files.readAllBytes(configFile);
        } catch (IOException e) {
            throw new RuntimeException("Could not load contents of kubeConfig file", e);
        }
        String base64ConfigContents = Base64.getEncoder().encodeToString(configContents);
        String clusterNamespace = "sock-shop";
        String podName = "carts-db-545d8475df-pvr6h";
        String containerId = "carts-db";
        KubernetesUpdate kubernetesUpdate = new KubernetesUpdate();
        kubernetesUpdate.update(base64ConfigContents, clusterNamespace, podName, containerId);
        System.exit(0);
    }

    @WebMethod
    @SOAPBinding
    @Oneway
    public void update(@WebParam(name = "kubeConfigContents", targetNamespace = "http://nodetypeimplementations.opentosca.org/") String kubeConfigContents,
                       @WebParam(name = "clusterNamespace", targetNamespace = "http://nodetypeimplementations.opentosca.org/") String clusterNamespace,
                       @WebParam(name = "PodName", targetNamespace = "http://nodetypeimplementations.opentosca.org/") String podName,
                       @WebParam(name = "ContainerID", targetNamespace = "http://nodetypeimplementations.opentosca.org/") String containerId) throws IOException {
        Path kubeConfigPath;
        try {
            kubeConfigPath = Files.createTempFile("kubeConfig", "");
            byte[] configContents = Base64.getDecoder().decode(kubeConfigContents);
            Files.write(kubeConfigPath, configContents);

            LOG.info("Written kubeConfig contents to |{}|", kubeConfigPath);
        } catch (IOException e) {
            LOG.error("Cannot write kubeConfig contents to file", e);
            throw e;
        }
        LOG.info("Updating container |{}| at |{}|", containerId, Instant.now());

        ApiClient apiClient = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath.toFile())))
                .build();
        Exec exec = new Exec(apiClient);
        try {
            Process proc = exec.exec(clusterNamespace, podName, new String[] {"bash", "-c", "echo test"}, containerId, false, false);

            Thread out =
                    new Thread(
                            () -> {
                                try {
                                    Streams.copy(proc.getInputStream(), System.out);
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            });
            out.start();

            Thread err =
                    new Thread(
                            () -> {
                                try {
                                    Streams.copy(proc.getErrorStream(), System.err);
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            });
            err.start();

            proc.waitFor();

            // wait for any last output; no need to wait for input thread
            out.join();
            err.join();

            proc.destroy();
            int exitCode = proc.exitValue();
            LOG.info("Process exited with |{}|", exitCode);
            HashMap<String, String> responseParams = new HashMap<>();
            responseParams.put("Response", "Exit code: " + exitCode);
            sendResponse(responseParams);
        } catch (InterruptedException e) {
            LOG.error("Error in exec call", e);
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }
}
