﻿using Azure;
using Azure.Storage.Blobs;
using Azure.Storage.Blobs.Models;
using Azure.Storage.Blobs.Specialized;

namespace BlobDevGuide
{
    class CopyBlob
    {
        //-------------------------------------------------
        // Check blob copy status
        //-------------------------------------------------
        // <Snippet_CheckStatusCopyBlob>
        public static async Task CheckCopyStatusAsync(CopyFromUriOperation copyOperation)
        {
            // Check for the latest status of the copy operation
            Response response = await copyOperation.UpdateStatusAsync();

            // Parse the response to find x-ms-copy-status header
            if (response.Headers.TryGetValue("x-ms-copy-status", out string value))
                Console.WriteLine($"Copy status: {value}");
        }
        //-------------------------------------------------
        // Copy a blob
        //-------------------------------------------------
        // <Snippet_CopyBlob>
        public static async Task CopyBlobAsync(BlobServiceClient blobServiceClient)
        {
            // Instantiate BlobClient for the source blob and destination blob
            BlobClient sourceBlob = blobServiceClient
                .GetBlobContainerClient("source-container")
                .GetBlobClient("sample-blob.txt");
            BlobClient destinationBlob = blobServiceClient
                .GetBlobContainerClient("destination-container")
                .GetBlobClient("sample-blob.txt");
            try
            {
                // Lease the source blob for the copy operation 
                // to prevent another client from modifying it
                BlobLeaseClient lease = sourceBlob.GetBlobLeaseClient();

                // Specifying -1 for the lease interval creates an infinite lease
                await lease.AcquireAsync(TimeSpan.FromSeconds(-1));

                // Start the copy operation and wait for it to complete
                CopyFromUriOperation copyOperation = await destinationBlob.StartCopyFromUriAsync(sourceBlob.Uri);
                await AbortBlobCopyAsync(copyOperation, destinationBlob);
                await copyOperation.WaitForCompletionAsync();

                // Release the lease on the source blob
                await lease.ReleaseAsync();
            }
            catch (RequestFailedException ex)
            {
                Console.WriteLine(ex.Message);
                Console.ReadLine();
                throw;
            }
        }
        // </Snippet_CopyBlob>

        //-------------------------------------------------
        // Abort a blob copy operation
        //-------------------------------------------------
        // <Snippet_AbortBlobCopy>
        public static async Task AbortBlobCopyAsync(
            CopyFromUriOperation copyOperation,
            BlobClient destinationBlob)
        {
            // Check for the latest status of the copy operation
            Response response = await copyOperation.UpdateStatusAsync();

            // Parse the response to find x-ms-copy-status header
            if (response.Headers.TryGetValue("x-ms-copy-status", out string value))
            {
                if (value == "pending")
                {
                    try
                    {
                        await destinationBlob.AbortCopyFromUriAsync(copyOperation.Id);
                        Console.WriteLine($"Copy operation {copyOperation.Id} aborted");
                    }

                    catch (RequestFailedException ex)
                    {
                        Console.WriteLine(ex.Message);
                        Console.ReadLine();
                        throw;
                    }
                }
            }
        }
        // </Snippet_AbortBlobCopy>
    }
}
