package com.datalake.manage;

import java.io.BufferedReader;

import java.io.InputStreamReader;

import java.security.InvalidKeyException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.azure.core.http.rest.Response;
import com.azure.storage.file.datalake.DataLakeDirectoryClient;
import com.azure.storage.file.datalake.DataLakeFileClient;
import com.azure.storage.file.datalake.DataLakeFileSystemClient;
import com.azure.storage.file.datalake.DataLakeServiceClient;
import com.azure.storage.file.datalake.models.AccessControlChangeCounters;
import com.azure.storage.file.datalake.models.AccessControlChangeResult;
import com.azure.storage.file.datalake.models.AccessControlType;
import com.azure.storage.file.datalake.models.PathAccessControl;
import com.azure.storage.file.datalake.models.PathAccessControlEntry;
import com.azure.storage.file.datalake.models.PathPermissions;
import com.azure.storage.file.datalake.models.PathRemoveAccessControlEntry;
import com.azure.storage.file.datalake.models.RolePermissions;
import com.azure.storage.file.datalake.options.PathSetAccessControlRecursiveOptions;

public class ACL_DataLake {
    
    // ----------------------------------------------------------
    // Get a file system
    // ----------------------------------------------------------

    public DataLakeFileSystemClient GetFileSystem
       (DataLakeServiceClient serviceClient, String fileSystemName){
        
        DataLakeFileSystemClient fileSystemClient =
            serviceClient.getFileSystemClient(fileSystemName);

        return fileSystemClient;
    }
    
    // ----------------------------------------------------------
    // Get and set directory-level permissions
    // ----------------------------------------------------------

    public void ManageDirectoryACLs(DataLakeFileSystemClient fileSystemClient){

        DataLakeDirectoryClient directoryClient =
          fileSystemClient.getDirectoryClient("");

        PathAccessControl directoryAccessControl =
            directoryClient.getAccessControl();

        List<PathAccessControlEntry> pathPermissions = directoryAccessControl.getAccessControlList();
       
        System.out.println(PathAccessControlEntry.serializeList(pathPermissions));
             
        RolePermissions groupPermission = new RolePermissions();
        groupPermission.setExecutePermission(true).setReadPermission(true);
  
        RolePermissions ownerPermission = new RolePermissions();
        ownerPermission.setExecutePermission(true).setReadPermission(true).setWritePermission(true);
  
        RolePermissions otherPermission = new RolePermissions();
        otherPermission.setReadPermission(true);
  
        PathPermissions permissions = new PathPermissions();
  
        permissions.setGroup(groupPermission);
        permissions.setOwner(ownerPermission);
        permissions.setOther(otherPermission);

        directoryClient.setPermissions(permissions, null, null);

        pathPermissions = directoryClient.getAccessControl().getAccessControlList();
     
        System.out.println(PathAccessControlEntry.serializeList(pathPermissions));

    }

    // ----------------------------------------------------------
    // Get and set file-level permissions
    // ----------------------------------------------------------

    public void ManageFileACLs(DataLakeFileSystemClient fileSystemClient){

        DataLakeDirectoryClient directoryClient =
          fileSystemClient.getDirectoryClient("my-directory");

        DataLakeFileClient fileClient = 
          directoryClient.getFileClient("uploaded-file.txt");

        PathAccessControl fileAccessControl =
            fileClient.getAccessControl();

      List<PathAccessControlEntry> pathPermissions = fileAccessControl.getAccessControlList();
     
      System.out.println(PathAccessControlEntry.serializeList(pathPermissions));
           
      RolePermissions groupPermission = new RolePermissions();
      groupPermission.setExecutePermission(true).setReadPermission(true);

      RolePermissions ownerPermission = new RolePermissions();
      ownerPermission.setExecutePermission(true).setReadPermission(true).setWritePermission(true);

      RolePermissions otherPermission = new RolePermissions();
      otherPermission.setReadPermission(true);

      PathPermissions permissions = new PathPermissions();

      permissions.setGroup(groupPermission);
      permissions.setOwner(ownerPermission);
      permissions.setOther(otherPermission);

      fileClient.setPermissions(permissions, null, null);

      pathPermissions = fileClient.getAccessControl().getAccessControlList();
   
      System.out.println(PathAccessControlEntry.serializeList(pathPermissions));

    }

    //-------------------------------------------------
    // Set ACLs recursively
    //-------------------------------------------------

    public void SetACLRecursively(DataLakeFileSystemClient fileSystemClient, Boolean isDefaultScope){
        
        DataLakeDirectoryClient directoryClient =
            fileSystemClient.getDirectoryClient("my-parent-directory");

        List<PathAccessControlEntry> pathAccessControlEntries = 
            new ArrayList<PathAccessControlEntry>();

        // Create owner entry.
        PathAccessControlEntry ownerEntry = new PathAccessControlEntry();

        RolePermissions ownerPermission = new RolePermissions();
        ownerPermission.setExecutePermission(true).setReadPermission(true).setWritePermission(true);

        ownerEntry.setDefaultScope(isDefaultScope);
        ownerEntry.setAccessControlType(AccessControlType.USER);
        ownerEntry.setPermissions(ownerPermission);

        pathAccessControlEntries.add(ownerEntry);

        // Create group entry.
        PathAccessControlEntry groupEntry = new PathAccessControlEntry();

        RolePermissions groupPermission = new RolePermissions();
        groupPermission.setExecutePermission(true).setReadPermission(true).setWritePermission(false);

        groupEntry.setDefaultScope(isDefaultScope);
        groupEntry.setAccessControlType(AccessControlType.GROUP);
        groupEntry.setPermissions(groupPermission);

        pathAccessControlEntries.add(groupEntry);

        // Create other entry.
        PathAccessControlEntry otherEntry = new PathAccessControlEntry();

        RolePermissions otherPermission = new RolePermissions();
        otherPermission.setExecutePermission(false).setReadPermission(false).setWritePermission(false);

        otherEntry.setDefaultScope(isDefaultScope);
        otherEntry.setAccessControlType(AccessControlType.OTHER);
        otherEntry.setPermissions(otherPermission);

        pathAccessControlEntries.add(otherEntry);

        // Create named user entry.
        PathAccessControlEntry userEntry = new PathAccessControlEntry();

        RolePermissions userPermission = new RolePermissions();
        userPermission.setExecutePermission(true).setReadPermission(true).setWritePermission(false);

        userEntry.setDefaultScope(isDefaultScope);
        userEntry.setAccessControlType(AccessControlType.USER);
        userEntry.setEntityId("4a9028cf-f779-4032-b09d-970ebe3db258");
        userEntry.setPermissions(userPermission);    
        
        pathAccessControlEntries.add(userEntry);
        
        directoryClient.setAccessControlRecursive(pathAccessControlEntries);        

    }

    //-------------------------------------------------
    // Update ACLs recursively
    //-------------------------------------------------

    public void UpdateACLRecursively(DataLakeFileSystemClient fileSystemClient, Boolean isDefaultScope){

        DataLakeDirectoryClient directoryClient =
        fileSystemClient.getDirectoryClient("my-parent-directory");

        List<PathAccessControlEntry> pathAccessControlEntries = 
            new ArrayList<PathAccessControlEntry>();

        // Create named user entry.
        PathAccessControlEntry userEntry = new PathAccessControlEntry();

        RolePermissions userPermission = new RolePermissions();
        userPermission.setExecutePermission(true).setReadPermission(true).setWritePermission(true);

        userEntry.setDefaultScope(isDefaultScope);
        userEntry.setAccessControlType(AccessControlType.USER);
        userEntry.setEntityId("4a9028cf-f779-4032-b09d-970ebe3db258");
        userEntry.setPermissions(userPermission);    
        
        pathAccessControlEntries.add(userEntry);
        
        directoryClient.updateAccessControlRecursive(pathAccessControlEntries);        
    
    }

    //-------------------------------------------------
    // Remove ACLs recursively
    //-------------------------------------------------

    public void RemoveACLRecursively(DataLakeFileSystemClient fileSystemClient, Boolean isDefaultScope){

        DataLakeDirectoryClient directoryClient =
        fileSystemClient.getDirectoryClient("my-parent-directory");
  
        List<PathRemoveAccessControlEntry> pathRemoveAccessControlEntries = 
            new ArrayList<PathRemoveAccessControlEntry>();

        // Create named user entry.
        PathRemoveAccessControlEntry userEntry = new PathRemoveAccessControlEntry();

        RolePermissions userPermission = new RolePermissions();
        userPermission.setExecutePermission(true).setReadPermission(true).setWritePermission(true);

        userEntry.setDefaultScope(isDefaultScope);
        userEntry.setAccessControlType(AccessControlType.USER);
        userEntry.setEntityId("4a9028cf-f779-4032-b09d-970ebe3db258"); 
        
        pathRemoveAccessControlEntries.add(userEntry);
        
        directoryClient.removeAccessControlRecursive(pathRemoveAccessControlEntries);      
    
    }

    //--------------------------------------------------
    // Use continuation token
    //--------------------------------------------------

    public String ResumeSetACLRecursively(DataLakeFileSystemClient fileSystemClient,
        DataLakeDirectoryClient directoryClient,
        List<PathAccessControlEntry> accessControlList, 
        String continuationToken){

        try{
            PathSetAccessControlRecursiveOptions options = new PathSetAccessControlRecursiveOptions(accessControlList);
            
            options.setContinuationToken(continuationToken);
        
           Response<AccessControlChangeResult> accessControlChangeResult =  
              directoryClient.setAccessControlRecursiveWithResponse(options, null, null);

           if (accessControlChangeResult.getValue().getCounters().getFailedChangesCount() > 0)
           {
              continuationToken =
                  accessControlChangeResult.getValue().getContinuationToken();
           }
        
           return continuationToken;

        }
        catch(Exception ex){
        
            System.out.println(ex.toString());
            return continuationToken;
        }


    }

    //--------------------------------------------------
    // Continue on failure
    //--------------------------------------------------

    public void ContinueOnFailure(DataLakeFileSystemClient fileSystemClient,
        DataLakeDirectoryClient directoryClient,
        List<PathAccessControlEntry> accessControlList){
        
        PathSetAccessControlRecursiveOptions options = 
           new PathSetAccessControlRecursiveOptions(accessControlList);
            
        options.setContinueOnFailure(true);
        
        Response<AccessControlChangeResult> accessControlChangeResult =  
            directoryClient.setAccessControlRecursiveWithResponse(options, null, null);

        AccessControlChangeCounters counters = accessControlChangeResult.getValue().getCounters();

        System.out.println("Number of directories changes: " + 
            counters.getChangedDirectoriesCount());

        System.out.println("Number of files changed: " + 
            counters.getChangedDirectoriesCount());

        System.out.println("Number of failures: " + 
            counters.getChangedDirectoriesCount());
    }

    // ----------------------------------------------------------
    // Driver menu
    // ----------------------------------------------------------

    public void ShowMenu() throws java.lang.Exception, URISyntaxException, InvalidKeyException{
    
        try {
            DataLakeServiceClient dataLakeServiceClient = Authorize_DataLake.GetDataLakeServiceClient
            (Constants.storageAccountName, Constants.accountKey);

            // Uncomment if you want to test AD Authorization.
            // DataLakeServiceClient dataLakeServiceClient = Authorize_DataLake.GetDataLakeServiceClient
            //    (accountName, clientID, clientSecret, tenantID);

            DataLakeFileSystemClient fileSystemClient = GetFileSystem(dataLakeServiceClient, Constants.containerName);

            // Listening for commands from the console
            System.out.print("\033[H\033[2J");  
            System.out.flush();

            System.out.println("Enter a command");

            System.out.println("(1) Manage directory ACL | (2) Manage File ACLs | (3) Set ACL recursive | " +
                            "(4) Update ACL recursive | (5) Remove ACL recursive | (6) Resume ACL recursive | (7) Exit");

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            while (true) {

                System.out.println("# Enter a command : ");
                String input = reader.readLine();

                switch(input){

                    case "1":
                        ManageDirectoryACLs(fileSystemClient);
                        break;
                    case "2":
                        ManageFileACLs(fileSystemClient);
                    break;
                    case "3":
                        SetACLRecursively(fileSystemClient, false);
                    break;
                    case "4":
                        UpdateACLRecursively(fileSystemClient, false);
                    break;
                    case "5":
                        RemoveACLRecursively(fileSystemClient, false);
                    break;
                    case "6":
                        DataLakeDirectoryClient directoryClient =
                        fileSystemClient.getDirectoryClient("my-parent-directory");

                        List<PathAccessControlEntry> pathAccessControlEntries = 
                            new ArrayList<PathAccessControlEntry>();

                        // Create owner entry.
                        PathAccessControlEntry ownerEntry = new PathAccessControlEntry();

                        RolePermissions ownerPermission = new RolePermissions();
                        ownerPermission.setExecutePermission(true).setReadPermission(true).setWritePermission(true);

                        ownerEntry.setAccessControlType(AccessControlType.USER);
                        ownerEntry.setPermissions(ownerPermission);

                        pathAccessControlEntries.add(ownerEntry);

                        // Create group entry.
                        PathAccessControlEntry groupEntry = new PathAccessControlEntry();

                        RolePermissions groupPermission = new RolePermissions();
                        groupPermission.setExecutePermission(true).setReadPermission(true).setWritePermission(false);

                        groupEntry.setAccessControlType(AccessControlType.GROUP);
                        groupEntry.setPermissions(groupPermission);

                        pathAccessControlEntries.add(groupEntry);

                        // Create other entry.
                        PathAccessControlEntry otherEntry = new PathAccessControlEntry();

                        RolePermissions otherPermission = new RolePermissions();
                        otherPermission.setExecutePermission(false).setReadPermission(false).setWritePermission(false);

                        otherEntry.setAccessControlType(AccessControlType.OTHER);
                        otherEntry.setPermissions(otherPermission);

                        pathAccessControlEntries.add(otherEntry);

                        // Create named user entry.
                        PathAccessControlEntry userEntry = new PathAccessControlEntry();

                        RolePermissions userPermission = new RolePermissions();
                        userPermission.setExecutePermission(true).setReadPermission(true).setWritePermission(false);

                        userEntry.setAccessControlType(AccessControlType.USER);
                        userEntry.setEntityId("4a9028cf-f779-4032-b09d-970ebe3db258");
                        userEntry.setPermissions(userPermission);    
                    
                        pathAccessControlEntries.add(userEntry);

                    //      String continuationToken = null;

                    //    continuationToken = ResumeSetACLRecursively
                    //        (fileSystemClient, directoryClient, pathAccessControlEntries, continuationToken);

                        ContinueOnFailure(fileSystemClient, directoryClient, pathAccessControlEntries);

                    break;
                    case "7":
                        return;
                    default:
                        break;
                    }
                }
        } catch (java.lang.Exception e) {
            System.out.println(e.toString());
            System.exit(-1);

        }
    }
}