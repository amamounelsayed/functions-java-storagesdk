package com.function;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;

import com.microsoft.azure.storage.blob.*;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Function {

    static CloudBlobClient blobClient = null;
    static ReentrantLock lock = new ReentrantLock();
    static final String storageConnection = System.getenv("AzureWebJobsStorage");

    @FunctionName("HttpTrigger-Java")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = { HttpMethod.GET,
            HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context)
            throws InvalidKeyException, URISyntaxException, StorageException, IOException {
        context.getLogger().info("Java HTTP trigger processed a request.");

        CloudBlobContainer container = GetBlobClient(context.getLogger()).getContainerReference("input");
        CloudBlockBlob blob = container.getBlockBlobReference("blobInput.json");
        
        String payload = blob.downloadText();

        context.getLogger().info("blob length: " + payload.length());

        return request.createResponseBuilder(HttpStatus.OK)
        .header("Content-Type", "application/json")
        .body(payload)
        .build();
    }

    private static CloudBlobClient GetBlobClient(Logger log) throws InvalidKeyException, URISyntaxException
    {
        if (blobClient != null) {
            return blobClient;
        }
        else {
            lock.lock();
            try { 
                if(blobClient == null)  {
                    log.info("Creating storage connection...");
                    CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnection);
                    CloudBlobClient bc = storageAccount.createCloudBlobClient();
                    blobClient = bc;
                    return blobClient;
                } 
                else {
                    return blobClient;
                }
            } finally {
                lock.unlock();
            }
        }
    }
}
