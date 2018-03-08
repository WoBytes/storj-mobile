package storjlib.services;

import android.app.IntentService;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.facebook.react.bridge.ReactContext;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.List;

import io.storj.libstorj.CreateBucketCallback;
import io.storj.libstorj.DeleteBucketCallback;
import io.storj.libstorj.DeleteFileCallback;
import io.storj.libstorj.Storj;
import storjlib.GsonSingle;
import storjlib.Models.BucketModel;
import storjlib.Models.FileModel;
import storjlib.Responses.Response;
import storjlib.Responses.SingleResponse;
import storjlib.dataProvider.DatabaseFactory;
import storjlib.dataProvider.Dbo.BucketDbo;
import storjlib.dataProvider.Dbo.FileDbo;
import storjlib.dataProvider.repositories.BucketRepository;
import storjlib.dataProvider.repositories.FileRepository;
import io.storj.libstorj.Bucket;
import io.storj.libstorj.File;
import io.storj.libstorj.GetBucketsCallback;
import io.storj.libstorj.ListFilesCallback;
import io.storj.libstorj.android.StorjAndroid;

import static storjlib.services.ServiceModule.GET_BUCKETS;
import static storjlib.services.ServiceModule.GET_FILES;
import static storjlib.services.ServiceModule.BUCKET_CREATED;
import static storjlib.services.ServiceModule.BUCKET_DELETED;
import static storjlib.services.ServiceModule.FILE_DELETED;

/**
 * Created by Yaroslav-Note on 3/6/2018.
 */

public class GetBucketsService extends IntentService {

    private final static String EVENT_BUCKETS_UPDATED = "EVENT_BUCKETS_UPDATED";
    private final static String EVENT_FILES_UPDATED = "EVENT_FILES_UPDATED";
    private final static String EVENT_BUCKET_CREATED = "EVENT_BUCKET_CREATED";
    private final static String EVENT_BUCKET_DELETED = "EVENT_BUCKET_DELETED";
    private final static String EVENT_FILE_DELETED = "EVENT_FILE_DELETED";

    private Bucket[] _buckets;
    private File[] _files;
    private ReactContext mContext;
    private IBinder mBinder = new GetBucketsServiceBinder();

    private final SQLiteDatabase _db;
    private final BucketRepository _bRepository;
    private final FileRepository _fRepository;
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public GetBucketsService() {

        super("GetBucketsService");
        _db = new DatabaseFactory(GetBucketsService.this, null).getWritableDatabase();
        _fRepository = new FileRepository(_db);
        _bRepository = new BucketRepository(_db);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String action = intent.getAction();

        switch(action) {
            case GET_BUCKETS:
                getBuckets();
                break;
            case GET_FILES:
                getFiles(intent.getStringExtra("bucketId"));
                break;
            case BUCKET_CREATED:
                createBucket(intent.getStringExtra("bucketName"));
                break;
            case BUCKET_DELETED:
                deleteBucket(intent.getStringExtra("bucketId"));
                break;
            case FILE_DELETED:
                deleteFile(intent.getStringExtra("bucketId"), intent.getStringExtra("fileId"));
                break;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void getBuckets() {
        getInstance().getBuckets(new GetBucketsCallback() {
            @Override
            public void onBucketsReceived(Bucket[] buckets) {
                _buckets = buckets;

                if(buckets == null) {
                    return;
                }

                _db.beginTransaction();

                try {
                    List<BucketDbo> bucketDbos = _bRepository.getAll();

                    int length = buckets.length;

                    outer:
                    for(BucketDbo bucketDbo : bucketDbos) {
                        int i = 0;
                        String dboId = bucketDbo.getId();

                        do {
                            Bucket bucket = buckets[i];
                            String id = bucket.getId();

                            if(dboId == id) {
                                _bRepository.update(new BucketModel(bucket));
                                arrayShift(buckets, i, length);
                                length--;
                                continue outer;
                            }

                            i++;
                        } while(i < length);

                        _bRepository.delete(dboId);
                    }

                    for(int i = 0; i < length; i ++) {
                        Response resp =_bRepository.insert(new BucketModel(buckets[i]));
                        if(!resp.isSuccess()) {
                            throw new Exception("Bucket insertion failed");
                        }
                    }

                    _db.setTransactionSuccessful();
                } catch (Exception e) {

                } finally {
                    _db.endTransaction();
                    _db.close();
                }

                if(mContext != null) {
                    mContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(EVENT_BUCKETS_UPDATED, true);
                }
            }

            @Override
            public void onError(int code, String message) {
                _buckets = null;

                if(mContext != null) {
                    mContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(EVENT_BUCKETS_UPDATED, false);
                }
            }
        });
    }

    private void getFiles(final String bucketId) {
        getInstance().listFiles(bucketId, new ListFilesCallback() {
            @Override
            public void onFilesReceived(File[] files) {
                _files = files;

                if(files == null) {
                    return;
                }

                _db.beginTransaction();

                try {
                    List<FileDbo> fileDbos = _fRepository.get(bucketId);

                    int length = files.length;
                    boolean[] isUpdate = new boolean[files.length];

                    outer:
                    for(FileDbo fileDbo : fileDbos) {
                        int i = 0;
                        String dboId = fileDbo.getId();

                        do {
                            File file = files[i];
                            String id = file.getId();

                            if(dboId == id) {
                                _fRepository.update(new FileModel(file));
                                arrayShift(files, i, length);
                                length--;
                                continue outer;
                            }

                            i++;
                        } while(i < length);

                        _fRepository.delete(dboId);
                    }

                    for(int i = 0; i < length; i ++) {
                        _fRepository.insert(new FileModel(files[i]));
                    }

                    _db.setTransactionSuccessful();
                } catch (Exception e) {
                    String s = e.getMessage();
                } finally {
                    _db.endTransaction();
                    _db.close();
                }

                if(mContext != null) {
                    mContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(EVENT_FILES_UPDATED, true);
                }
            }

            @Override
            public void onError(int code, String message) {
                _files = null;

                if(mContext != null) {
                    mContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(EVENT_FILES_UPDATED, false);
                }
            }
        });
    }

    private void createBucket(final String bucketName) {
        getInstance().createBucket(bucketName, new CreateBucketCallback() {
            @Override
            public void onBucketCreated(Bucket bucket) {
                Response insertionResponse = _bRepository.insert(new BucketModel(bucket));

                if(insertionResponse.isSuccess()){
                    mContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(EVENT_BUCKET_CREATED,
                            new SingleResponse(true, toJson(new BucketModel(bucket)),  null).toWritableMap());
                    return;
                }

                mContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(EVENT_BUCKET_CREATED,
                        new Response(false, "Bucket insertion to db failed").toWritableMap());
            }

            @Override
            public void onError(int code, String message) {
                mContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(EVENT_BUCKET_CREATED,
                        new Response(false, message, code).toWritableMap());
            }
        });
    }

    private void deleteBucket(final String bucketId) {
        getInstance().deleteBucket(bucketId, new DeleteBucketCallback() {
            @Override
            public void onBucketDeleted() {
                Response deletionResponse = _bRepository.delete(bucketId);

                if(deletionResponse.isSuccess()){
                    mContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(EVENT_BUCKET_DELETED,
                            new Response(true, null).toWritableMap());
                    return;
                }

                mContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(EVENT_BUCKET_DELETED,
                        new Response(false, "Bucket deletion failed in db").toWritableMap());
            }

            @Override
            public void onError(int code, String message) {
                mContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(EVENT_BUCKET_DELETED,
                        new Response(false, message, code).toWritableMap());
            }
        });
    }

    private void deleteFile(final String bucketId, final String fileId) {
        getInstance().deleteFile(bucketId, fileId, new DeleteFileCallback() {
            @Override
            public void onFileDeleted() {
                Response deletionResponse = _bRepository.delete(bucketId);

                if(deletionResponse.isSuccess()){
                    mContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(EVENT_FILE_DELETED,
                            new Response(true, null).toWritableMap());
                    return;
                }

                mContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(EVENT_FILE_DELETED,
                        new Response(false, "File deletion failed in db").toWritableMap());
            }

            @Override
            public void onError(int code, String message) {
                mContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(EVENT_FILE_DELETED,
                        new Response(false, message, code).toWritableMap());
            }
        });
    }

    private <T> void arrayShift(T[] array, int pos, int length) {
        do {
            array[pos] = array[pos + 1];
            pos++;
        }
        while(pos < length - 1);
    }

    private <T> String toJson(T convertible) {
        return GsonSingle.getInstanse().toJson(convertible);
    }

    private Storj getInstance() {
        return StorjAndroid.getInstance(this);
    }

    public class GetBucketsServiceBinder extends Binder {
        public GetBucketsService getService() { return GetBucketsService.this; }
    }

    public void setReactContext(ReactContext context) {
        mContext = context;
    }
}