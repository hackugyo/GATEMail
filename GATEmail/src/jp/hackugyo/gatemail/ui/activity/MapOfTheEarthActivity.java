package jp.hackugyo.gatemail.ui.activity;

import android.graphics.Color;
import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import jp.hackugyo.gatemail.R;
import jp.hackugyo.gatemail.model.withoutdb.MapField;
import jp.hackugyo.gatemail.ui.AbsFragmentActivity;
import jp.hackugyo.gatemail.util.LogUtils;

public class MapOfTheEarthActivity extends AbsFragmentActivity {
    @SuppressWarnings("unused")
    private final MapOfTheEarthActivity self = this;
    private GoogleMap mGoogleMap;

    /***********************************************
     * Life Cycle *
     ***********************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_of_the_earth);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleMap == null) {
            mGoogleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        }
        if (mGoogleMap != null) {
            mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID); // ラベルつき航空写真モード

            mGoogleMap.setOnMarkerClickListener(new OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    marker.showInfoWindow(); // ピンの上にバルーンを表示
                    return false;
                }
            });

            LatLng[] here = { //
            //
                    new LatLng(35.6472069, 139.7013787),//
                    new LatLng(35.6472069, 139.701), //
                    new LatLng(35.6471, 139.701),//
                    new LatLng(35.6471, 139.7013787),//
                    new LatLng(35.6473, 139.702),//
            };
            LogUtils.i("drawn");
            MapField field1 = new MapField("だいかんやま", here);
            drawField(field1);

            LatLng[] here2 = { //
            //
            new LatLng(35.64705, 139.70),//
            };
            LogUtils.i("drawn");
            MapField field2 = new MapField("だいかんやま", here2);
            field2.setColorRgb(128, 128, 0);
            drawField(field2);
        }
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    /***********************************************
     * draw Fields *
     ***********************************************/

    private void drawField(MapField field) {
        // マーカー定義
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.title(field.getName()); // 名前を設定 
        markerOptions.snippet(field.getMemo()); // 説明を設定
        // マーカーの座標を設定(区画の中心を自動算出) 
        markerOptions.position(calcCenter(field.getVertexes()));
        // 色を設定 
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(field.getColorHue()));
        // マップにマーカーを追加 
        mGoogleMap.addMarker(markerOptions);
        // 区画を描画
        final LatLng[] vertexes = field.getVertexes();
        if (vertexes == null || vertexes.length == 0) return;

        // RGBそれぞれの色を作成
        final int[] colorRgb = field.getColorRgb();
        int colorRed = colorRgb[0];
        int colorGreen = colorRgb[1];
        int colorBlue = colorRgb[2];
        if (vertexes.length == 1) {
            LogUtils.i("add circle");
            CircleOptions circleOptions = new CircleOptions();
            circleOptions.strokeColor(Color.argb(0x255, colorRed, colorGreen, colorBlue));
            circleOptions.strokeWidth(5);
            // 区画の塗りつぶしについて設定
            circleOptions.fillColor(Color.argb(0x40, colorRed, colorGreen, colorBlue));
            circleOptions.center(vertexes[0]);
            circleOptions.radius(50); // 半径（メートル）
            mGoogleMap.addCircle(circleOptions);

        } else if (vertexes.length >= 2) {
            LogUtils.i("add polygon");
            // ポリゴン定義
            PolygonOptions polygonOptions = new PolygonOptions();
            // 区画の輪郭について設定
            polygonOptions.strokeColor(Color.argb(0x255, colorRed, colorGreen, colorBlue));
            polygonOptions.strokeWidth(5);
            // 区画の塗りつぶしについて設定
            polygonOptions.fillColor(Color.argb(0x40, colorRed, colorGreen, colorBlue));
            // 各頂点の座標を設定（順番に！）
            polygonOptions.add(vertexes); // LatLng でも LatLng[] でも OK
            // マップにポリゴンを追加
            mGoogleMap.addPolygon(polygonOptions);
        }
    }

    private static LatLng calcCenter(LatLng[] positions) {

        if (positions.length == 0) return new LatLng(0d, 0d);
        if (positions.length == 1) return positions[0];

        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        for (int i = 0; i < positions.length; i++) {
            LatLng current = positions[i];
            if (current.latitude >= maxX) maxX = current.latitude;
            if (current.longitude >= maxY) maxY = current.longitude;
            if (current.latitude <= minX) minX = current.latitude;
            if (current.longitude <= minY) minY = current.longitude;
        }

        // 重心算出の近似式
        double centerX = minX + (maxX - minX) / 2;
        double centerY = minY + (maxY - minY) / 2;

        return new LatLng(centerX, centerY);
    }
}
