package com.function;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;

import com.azure.storage.blob.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Function {

    static BlobServiceClient blobServiceClient = null;
    static ReentrantLock lock = new ReentrantLock();
    static final String storageConnection = System.getenv("AzureWebJobsStorage");

    @FunctionName("HttpTrigger-Java")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = { HttpMethod.GET,
            HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context)
            throws InvalidKeyException, URISyntaxException, IOException {
        context.getLogger().info("Java HTTP trigger processed a request.");

        BlobContainerClient  container = getBlobClient(context.getLogger()).getBlobContainerClient("ahmed");
        BlobClient blobClient = container.getBlobClient("Dockerfile.java");
        
        int dataSize = (int) blobClient.getProperties().getBlobSize();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(dataSize);
        blobClient.download(outputStream);
        outputStream.close();

        String payload = new String(outputStream.toByteArray());

        context.getLogger().info("blob length: " + payload.length());

        return request.createResponseBuilder(HttpStatus.OK)
        .body(payload)
        .build();
    }

    private static BlobServiceClient getBlobClient(Logger log) throws InvalidKeyException, URISyntaxException
    {
        if (blobServiceClient != null) {
            return blobServiceClient;
        }
        else {
            lock.lock();
            try { 
                if(blobServiceClient == null)  {
                    log.info("Creating storage connection...");
                    BlobServiceClient blobClient  = new BlobServiceClientBuilder().connectionString(storageConnection).buildClient();
                    blobServiceClient = blobClient;
                    return blobServiceClient;
                } 
                else {
                    return blobServiceClient;
                }
            } finally {
                lock.unlock();
            }
        }
    }
}
