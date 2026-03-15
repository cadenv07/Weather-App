package com.swirlingskies.weatherapp.fragments;

import static androidx.core.content.ContentProviderCompat.requireContext;

import static org.maplibre.android.style.expressions.Expression.get;
import static org.maplibre.android.style.expressions.Expression.literal;
import static org.maplibre.android.style.expressions.Expression.match;
import static org.maplibre.android.style.expressions.Expression.stop;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.geometry.LatLngBounds;
import org.maplibre.android.maps.MapLibreMapOptions;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.Style;
import org.maplibre.android.style.expressions.Expression;
import org.maplibre.android.style.layers.FillLayer;
import org.maplibre.android.style.layers.LineLayer;
import org.maplibre.android.style.layers.PropertyFactory;
import org.maplibre.android.style.sources.GeoJsonSource;

import java.net.URI;

public class MapFragment extends Fragment {

    private MapView mapView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mapView = new MapView(requireContext(), MapLibreMapOptions.createFromAttributes(requireContext()));
        return mapView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(map -> {
            map.getUiSettings().setAttributionEnabled(false);
            map.getUiSettings().setLogoEnabled(false);
            LatLngBounds conusBounds = new LatLngBounds.Builder()
                    .include(new LatLng(49.5, -125.0))
                    .include(new LatLng(24.0, -66.0))
                    .build();

            map.setLatLngBoundsForCameraTarget(conusBounds);
            map.setMinZoomPreference(3.5);

            CameraPosition initial = new CameraPosition.Builder()
                    .target(new LatLng(38.5, -98.0))
                    .zoom(3.5)
                    .build();

            map.setCameraPosition(initial);
            map.setStyle(buildStyle(), this::addAwipsLayers);
        });
    }

    private Style.Builder buildStyle() {
        return new Style.Builder().fromUri("asset://dark.json");
    }

    private void addAwipsLayers(Style style) {
        style.addImage("hatch-1", createHatchBitmap(1));
        style.addImage("hatch-2", createHatchBitmap(2));
        style.addImage("hatch-3", createHatchBitmap(3));

        style.addSource(new GeoJsonSource("counties-src", URI.create("asset://counties.geojson")));
        LineLayer countiesLayer = new LineLayer("counties-layer", "counties-src");
        countiesLayer.setMinZoom(5f);
        countiesLayer.setProperties(
                PropertyFactory.lineColor("#444444"),
                PropertyFactory.lineWidth(0.5f),
                PropertyFactory.lineOpacity(0.8f)
        );
        style.addLayer(countiesLayer);

        style.addSource(new GeoJsonSource("world-src", URI.create("asset://world.geojson")));
        LineLayer worldLayer = new LineLayer("world-layer", "world-src");
        worldLayer.setProperties(
                PropertyFactory.lineColor("#AAAAAA"),
                PropertyFactory.lineWidth(0.5f),
                PropertyFactory.lineOpacity(0.8f)
        );
        style.addLayer(worldLayer);

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, "http://awips-server:8080/spc/11",
                response -> {
                    style.addSource(new GeoJsonSource("spc-outlook-source", response));
                    FillLayer spcFill = new FillLayer("spc-outlook-fill", "spc-outlook-source");
                    spcFill.setFilter(
                            Expression.all(
                                    Expression.not(Expression.eq(get("label"), literal("CIG1"))),
                                    Expression.not(Expression.eq(get("label"), literal("CIG2"))),
                                    Expression.not(Expression.eq(get("label"), literal("CIG3")))
                            )
                    );
                    spcFill.setProperties(
                            PropertyFactory.fillColor(get("fill")),
                            PropertyFactory.fillOpacity(0.4f)
                    );
                    style.addLayer(spcFill);
                    LineLayer spcLine = new LineLayer("spc-outlook-line", "spc-outlook-source");
                    spcLine.setProperties(
                            PropertyFactory.lineColor(get("stroke")),
                            PropertyFactory.lineWidth(1.5f)
                    );
                    style.addLayer(spcLine);
                    FillLayer spcHatch = new FillLayer("spc-wind-hatch", "spc-outlook-source");
                    spcHatch.setFilter(
                            Expression.any(
                                    Expression.eq(get("label"), literal("CIG1")),
                                    Expression.eq(get("label"), literal("CIG2")),
                                    Expression.eq(get("label"), literal("CIG3"))
                            )
                    );
                    spcHatch.setProperties(
                            PropertyFactory.fillPattern(
                                    match(get("label"), literal("hatch-1"),
                                            stop("CIG1", "hatch-1"),
                                            stop("CIG2", "hatch-2"),
                                            stop("CIG3", "hatch-3")
                                    )
                            )
                    );
                    style.addLayer(spcHatch);
                },
                error -> Log.e("Volley", "Error: " + error.toString())
        );
        queue.add(stringRequest);
    }

    @Override
    public void onStart() { super.onStart(); mapView.onStart(); }

    @Override
    public void onResume() { super.onResume(); mapView.onResume(); }

    @Override
    public void onPause() { super.onPause(); mapView.onPause(); }

    @Override
    public void onStop() { super.onStop(); mapView.onStop(); }

    @Override
    public void onDestroyView() { super.onDestroyView(); mapView.onDestroy(); }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    private Bitmap createHatchBitmap(int density) {
        int size = 20;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2.f);
        paint.setAntiAlias(true);

        switch (density) {
            case 1: // sparse single diagonal - one line per tile, bottom-left to top-right
                canvas.drawLine(0, size/2.f, size/2.f, 0, paint);
                break;
            case 2: // top-left to bottom-right diagonal
                canvas.drawLine(-1, -1, size+1, size+1, paint);
                break;
            case 3: // crosshatch
                canvas.drawLine(0, size, size, 0, paint);
                canvas.drawLine(0, 0, size, size, paint);
                break;
        }
        return bitmap;
    }
}