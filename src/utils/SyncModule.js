import { NativeModules } from 'react-native';

const SyncModule = (() => {
    let instance = null;
    const syncModule = NativeModules.SyncModule;

    class SyncModule {
        constructor() {

        }

        async listBuckets() {
            return await syncModule.listBuckets();
        }

        async listFiles(bucketId) {
            return await syncModule.listFiles(bucketId);
        }

        async listAllFiles() {
            return await syncModule.listAllFiles();
        }

        async listUploadingFiles(bucketId) {
            return await syncModule.listUploadingFiles(bucketId);
        }

        async getUploadingFile(fileHandle) {
            return await syncModule.getUploadingFile(String(fileHandle));
        }

        async getFile(fileId) {
            return await syncModule.getFile(fileId);
        }

        async updateBucketStarred(bucketId, isStarred) {
            return await syncModule.updateBucketStarred(bucketId, isStarred);
        }

        async updateFileStarred(fileId, isStarred) {
            return await syncModule.updateFileStarred(fileId, isStarred);
        }

        async listSettings(settingsId) {
            return await syncModule.listSettings(String(settingsId));
        }

        async updateSyncSettings(settingsId, syncSettings) {
            return await syncModule.updateSyncSettings(String(settingsId), Number(syncSettings));
        }

        async changeSyncStatus(settingsId, value) {
            return await syncModule.changeSyncStatus(String(settingsId), Boolean(value));
        }
    }  

    return {
        /**
         * @returns {SyncModule}
         */
        getInstance: function() {
            if(!instance) {
                instance = new SyncModule();
            }

            return instance;
        }
    };

})();

export default SyncModule.getInstance();