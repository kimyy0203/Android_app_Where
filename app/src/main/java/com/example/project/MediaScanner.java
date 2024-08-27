package com.example.project;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.text.TextUtils;
/*안드로이드 애플리케이션에서 미디어 파일을 스캔하여 미디어 데이터베이스에 추가하는 기능을 제공한다.
  이를 통해, 파일을 시스템의 미디어 라이브러리에 등록하거나 갤러리와 같은 애플리케이션에서 즉시 사용할 수 있게
  할 수 있다. 이 클래스는 주로 싱글톤 패턴으로 설계되어 있으며, MediaScannerConnection을 사용하여 미디어 파일을 스캔한다*/
public class MediaScanner {
    private                 Context                                             mContext;
    private static volatile com.example.project.MediaScanner mMediaInstance = null;
    private                 MediaScannerConnection                              mMediaScanner;
    //private                 MediaScannerConnection.MediaScannerConnectionClient mMediaScannerClient;

    private String mFilePath;

    public static com.example.project.MediaScanner getInstance(Context context ) {
        if( null == context )
            return null;

        if( null == mMediaInstance )
            mMediaInstance = new com.example.project.MediaScanner( context );
        return mMediaInstance;
    }

    public static void releaseInstance() {
        if ( null != mMediaInstance ) {
            mMediaInstance = null;
        }
    }


    private MediaScanner(Context context) {
        mContext = context;

        mFilePath = "";

        MediaScannerConnection.MediaScannerConnectionClient mediaScanClient;
        mediaScanClient = new MediaScannerConnection.MediaScannerConnectionClient(){
            @Override public void onMediaScannerConnected() {
                mMediaScanner.scanFile(mFilePath, null);
//                mFilePath = path;
            }

            @Override public void onScanCompleted(String path, Uri uri) {
                System.out.println("::::MediaScan Success::::");

                mMediaScanner.disconnect();
            }
        };
        mMediaScanner = new MediaScannerConnection(mContext, mediaScanClient);
    }

    public void mediaScanning(final String path) {

        if( TextUtils.isEmpty(path) )
            return;
        mFilePath = path;

        if( !mMediaScanner.isConnected() )
            mMediaScanner.connect();

        //mMediaScanner.scanFile( path,null );
    }
}
