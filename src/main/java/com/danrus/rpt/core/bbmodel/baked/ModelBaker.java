package com.danrus.rpt.core.bbmodel.baked;

import com.danrus.bb4j.api.utils.RenderUtils;
import java.util.ArrayList;
import java.util.List;

public class ModelBaker {
    
    public static BakedModelData bakeModel(List<RenderUtils.RenderableMesh> rawMeshes) {
        List<RptBakedMesh> bakedMeshes = new ArrayList<>(rawMeshes.size());
        
        for (RenderUtils.RenderableMesh rawMesh : rawMeshes) {
            bakedMeshes.add(bakeMesh(rawMesh));
        }
        
        return new BakedModelData(bakedMeshes);
    }
    
    public static RptBakedMesh bakeMesh(RenderUtils.RenderableMesh mesh) {
        double[] pos = mesh.getPosition();
        double[] origin = mesh.getLocalOrigin() != null ? mesh.getLocalOrigin() : mesh.getLocalCenter();
        double[] rot = mesh.getRotation();
        double[] scale = mesh.getScale() != null ? mesh.getScale() : new double[]{1.0, 1.0, 1.0};
        
        boolean hasRot = Math.abs(rot[0]) > 0.0001 || Math.abs(rot[1]) > 0.0001 || Math.abs(rot[2]) > 0.0001;
        boolean hasScale = Math.abs(scale[0] - 1.0) > 0.0001 || Math.abs(scale[1] - 1.0) > 0.0001 || Math.abs(scale[2] - 1.0) > 0.0001;
        
        List<RptBakedQuad> quads = new ArrayList<>();
        for (RenderUtils.RenderableFace face : mesh.getFaces()) {
            quads.add(bakeFace(face, mesh.getTextureUuid()));
        }
        
        List<RptBakedMesh.BakedTransformStep> steps = new ArrayList<>();
        if (mesh.getTransformSteps() != null) {
            for (RenderUtils.RenderableMesh.TransformStep s : mesh.getTransformSteps()) {
                steps.add(new RptBakedMesh.BakedTransformStep(
                        s.getUuid(),
                        (float)(s.getOrigin()[0] / 16.0), (float)(s.getOrigin()[1] / 16.0), (float)(s.getOrigin()[2] / 16.0),
                        (float)(s.getPosition()[0] / 16.0), (float)(s.getPosition()[1] / 16.0), (float)(s.getPosition()[2] / 16.0),
                        (float)s.getRotation()[0], (float)s.getRotation()[1], (float)s.getRotation()[2],
                        (float)s.getScale()[0], (float)s.getScale()[1], (float)s.getScale()[2]
                ));
            }
        }
        
        return new RptBakedMesh(
                (float)(pos[0] / 16.0), (float)(pos[1] / 16.0), (float)(pos[2] / 16.0),
                (float)(origin[0] / 16.0), (float)(origin[1] / 16.0), (float)(origin[2] / 16.0),
                (float)rot[0], (float)rot[1], (float)rot[2],
                (float)scale[0], (float)scale[1], (float)scale[2],
                hasRot, hasScale,
                quads,
                mesh.getHierarchy(),
                steps
        );
    }
    
    private static RptBakedQuad bakeFace(RenderUtils.RenderableFace face, String meshTextureUuid) {
        double[][] v = face.getVertices();
        double[][] uv = face.getVertexUvs();
        boolean hasUvs = uv != null && uv.length >= v.length;
        double[] uvBounds = hasUvs ? null : face.getUv();
        
        float u1 = uvBounds != null && uvBounds.length > 0 ? (float) uvBounds[0] : 0f;
        float v1 = uvBounds != null && uvBounds.length > 1 ? (float) uvBounds[1] : 0f;
        float u2 = uvBounds != null && uvBounds.length > 2 ? (float) uvBounds[2] : 1f;
        float v2 = uvBounds != null && uvBounds.length > 3 ? (float) uvBounds[3] : 1f;
        
        float[] uu = new float[4];
        float[] vv = new float[4];
        float[] xx = new float[4];
        float[] yy = new float[4];
        float[] zz = new float[4];
        
        for (int i = 0; i < 4; i++) {
            if (i < v.length) {
                xx[i] = (float) (v[i][0] / 16.0f);
                yy[i] = (float) (v[i][1] / 16.0f);
                zz[i] = (float) (v[i][2] / 16.0f);
            } else {
                // Duplicate last vertex if triangle
                xx[i] = xx[v.length - 1];
                yy[i] = yy[v.length - 1];
                zz[i] = zz[v.length - 1];
            }
            
            if (hasUvs && i < uv.length) {
                uu[i] = uv[i] != null && uv[i].length > 0 ? (float) uv[i][0] : 0f;
                vv[i] = uv[i] != null && uv[i].length > 1 ? (float) uv[i][1] : 0f;
            } else if (hasUvs) {
                uu[i] = uu[uv.length - 1];
                vv[i] = vv[uv.length - 1];
            } else {
                if (v.length == 4) {
                    uu[i] = (i == 1 || i == 2) ? u2 : u1;
                    vv[i] = (i == 2 || i == 3) ? v2 : v1;
                } else {
                    uu[i] = u1;
                    vv[i] = v1;
                }
            }
        }
        
        float[] norm = face.getNormal() != null
                ? new float[]{(float)face.getNormal()[0], (float)face.getNormal()[1], (float)face.getNormal()[2]}
                : calculateNormal(v);
                
        double[] localCenter = face.getLocalCenter();
        float cx = localCenter != null && localCenter.length >= 3 ? (float) (localCenter[0] / 16.0f) : 0f;
        float cy = localCenter != null && localCenter.length >= 3 ? (float) (localCenter[1] / 16.0f) : 0f;
        float cz = localCenter != null && localCenter.length >= 3 ? (float) (localCenter[2] / 16.0f) : 0f;
        
        String texRef = face.getTextureUuid() != null ? face.getTextureUuid() : meshTextureUuid;
        
        return new RptBakedQuad(
                xx[0], yy[0], zz[0], uu[0], vv[0],
                xx[1], yy[1], zz[1], uu[1], vv[1],
                xx[2], yy[2], zz[2], uu[2], vv[2],
                xx[3], yy[3], zz[3], uu[3], vv[3],
                norm[0], norm[1], norm[2],
                cx, cy, cz,
                texRef, texRef != null
        );
    }
    
    private static float[] calculateNormal(double[][] vertices) {
        if (vertices.length < 3) {
            return new float[]{0, 1, 0};
        }

        double[] v1 = vertices[0];
        double[] v2 = vertices[1];
        double[] v3 = vertices[2];

        float x = (float)((v2[1]-v1[1])*(v3[2]-v1[2]) - (v2[2]-v1[2])*(v3[1]-v1[1]));
        float y = (float)((v2[2]-v1[2])*(v3[0]-v1[0]) - (v2[0]-v1[0])*(v3[2]-v1[2]));
        float z = (float)((v2[0]-v1[0])*(v3[1]-v1[1]) - (v2[1]-v1[1])*(v3[0]-v1[0]));

        float length = (float)Math.sqrt(x*x + y*y + z*z);
        if (length > 0) {
            x /= length;
            y /= length;
            z /= length;
        }

        return new float[]{x, y, z};
    }
}
