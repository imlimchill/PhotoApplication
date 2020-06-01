package com.MrPhoto.photoapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionSupport {

    private Context context;
    private Activity activity;

    /**
     * 사용자에게 허용받아야 하는 권한의 배열
     */
    private String[] permissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private List<String> mPermissionList;

    /**
     * 권한 요청 시 결과값을 받기 위해 지정해주는 int / 원하는 값 지정
     */
    private final int MULTI_PERMISSION = 1001;

    /**
     * @param context
     * @param activity
     */
    public PermissionSupport(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    /**
     * 사용자에게 허용받아야 하는 권한이 있는지 체크
     *
     * @return 허용받아야할 권한의 유무
     */
    public boolean checkPermission() {
        int result;
        mPermissionList = new ArrayList<>();

        // 권한 배열에 저장되어 있는 권한을 하나씩 꺼내서 검사한다.
        for (String per : permissions) {
            result = ContextCompat.checkSelfPermission(context, per);
            if (result != PackageManager.PERMISSION_GRANTED) mPermissionList.add(per);
        }

        // 만약 권한 리스트에 권한이 들어있으면 false 리턴
        return mPermissionList.isEmpty();
    }

    /**
     * 권한 허용 요청하는 함수
     */
    public void requestPermissions() {
        // 빈 String 배열을 Parameter로 제공하는 경우 Array를 다시 만들어 줌.
        ActivityCompat.requestPermissions(activity, mPermissionList.toArray(new String[0]), MULTI_PERMISSION);
    }

    /**
     * 권한 요청에 대한 결과를 처리해서 반환하는 함수
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     * @return 권한 요청에 대한 결과
     */
    public boolean permissionResult(int requestCode, String[] permissions, int[] grantResults) {
        // requestCode가 결과값을 받기 위해 지정해둔 MULTI_PERMISSION 와 같은지 확인
        // 결과값의 크기가 0보다 큰지 확인
        if (requestCode == MULTI_PERMISSION && permissions != null && grantResults != null && permissions.length > 0 && grantResults.length > 0) {
            for (int i = 0; i < grantResults.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        }

        return true;
    }

//    public void finalCheckPermission() {
//        AlertDialog.Builder localbBuilder = new AlertDialog.Builder(activity);
//        localbBuilder.setTitle("권한 설정").setMessage("권한 거절로 인해 일부기능이 제한됩니다. 제한된 기능의 권한을 허용해주세요.")
//                .setPositiveButton("권한 설정하러 가기", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        try {
//                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:"+ activity.getPackageName()));
//                            activity.startActivity(intent);
//                        } catch (ActivityNotFoundException e) {
//                            e.printStackTrace();
//                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                            activity.startActivity(intent);
//                        }
//                    }
//                }).create().show();
//
//    }

}
