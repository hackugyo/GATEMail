package jp.hackugyo.gatemail.model.withoutdb;

import com.google.android.gms.maps.model.LatLng;

import java.util.Arrays;

public class MapField {
    @SuppressWarnings("unused")
    private final MapField self = this;

    private String mName;
    private String mMemo;
    private LatLng[] mVertexes;
    private int[] mColorRgb = new int[3];

    public MapField(String name, LatLng[] vertexes) {
        mName = name;
        mVertexes = vertexes;
        // default:green
        mColorRgb[0] = 0;
        mColorRgb[1] = 255;
        mColorRgb[2] = 0;
    }

    /***********************************************
     * convert color *
     **********************************************/

    /**
     * RGBの値から色相を返します。
     * 
     * @return 色相値
     */
    public float getColorHue() {
        float[] hsv = new float[3];
        if (mColorRgb == null) {
            android.graphics.Color.RGBToHSV(255, 0, 0, hsv); // default:red
        } else {
            android.graphics.Color.RGBToHSV(mColorRgb[0], mColorRgb[1], mColorRgb[2], hsv);
        }

        return hsv[0];
    }

    /***********************************************
     * equals / toString *
     **********************************************/

    @Override
    public String toString() {
        return "MapField [name=" + this.mName + ", memo=" + this.mMemo + ", vertexes=" + Arrays.toString(this.mVertexes) + ", colorRgb=" + Arrays.toString(this.mColorRgb) + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.mName == null) ? 0 : this.mName.hashCode());
        result = prime * result + Arrays.hashCode(this.mVertexes);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        MapField other = (MapField) obj;
        if (this.mName == null) {
            if (other.mName != null) return false;
        } else if (!this.mName.equals(other.mName)) return false;
        if (!Arrays.equals(this.mVertexes, other.mVertexes)) return false;
        return true;
    }

    /***********************************************
     * getter / setter *
     **********************************************/

    public String getName() {
        return this.mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getMemo() {
        return this.mMemo;
    }

    public void setMemo(String memo) {
        this.mMemo = memo;
    }

    public LatLng[] getVertexes() {
        return this.mVertexes;
    }

    public void setVertexes(LatLng[] vertexes) {
        this.mVertexes = vertexes;
    }

    public int[] getColorRgb() {
        return this.mColorRgb;
    }

    public void setColorRgb(int[] colorRgb) {
        this.mColorRgb = colorRgb;
    }

    public void setColorRgb(int red, int green, int blue) {
        int[] colorRgb = { red, green, blue };
        this.mColorRgb = colorRgb;
    }

}
