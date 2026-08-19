package com.leclowndu93150.thaumaturge.client.effect.geometry;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

public final class PolyCone {
    public static final int TESS = 8;
    private static final double DEGENERATE_TOLERANCE = 2.0E-6;

    private PolyCone() {}

    public static void render(PoseStack poseStack, VertexConsumer consumer, double[][] pointArray, float[][] colourArray, double[] radiusArray, int light, float texSlice, float start) {
        int npoints = pointArray.length;
        if (npoints < 3)
            return;
        if (radiusArray.length != npoints || colourArray.length != npoints)
            return;

        double[][][] xformArray = new double[npoints][2][3];
        for (int p = 0; p < npoints; p++) {
            xformArray[p][0][0] = radiusArray[p];
            xformArray[p][0][1] = 0.0;
            xformArray[p][1][0] = 0.0;
            xformArray[p][1][1] = radiusArray[p];
        }

        double[][] contour = new double[TESS][2];
        for (int k = 0; k < TESS; k++) {
            double angle = Math.PI * 2.0 * k / TESS;
            contour[k][0] = Math.cos(angle);
            contour[k][1] = Math.sin(angle);
        }

        double[] yup = pickInitialUp(npoints, pointArray);
        double[] origin = {0.0, 0.0, 0.0};
        double[] negZ = {0.0, 0.0, 0.0};

        int i = 0;
        int inext = 0;
        double[] diff = new double[3];
        inext = findNonDegeneratePoint(inext, npoints, diff, pointArray);
        double len = vecLen(diff);
        double lenSeg = len;
        double[] bi0 = bisectingPlane(pointArray[0], pointArray[1], pointArray[inext]);
        yup = vecReflect(yup, bi0);

        double[][] frontLoop = new double[TESS][3];
        double[][] backLoop = new double[TESS][3];

        Matrix4f mat = poseStack.last().pose();

        while (inext < npoints - 1) {
            int inextnext = inext;
            inextnext = findNonDegeneratePoint(inextnext, npoints, diff, pointArray);
            len = vecLen(diff);
            double[] bi2 = bisectingPlane(pointArray[i], pointArray[inext], pointArray[inextnext]);
            double[][] m = uviewpoint(pointArray[i], pointArray[inext], yup);
            double[] bisector0 = matDotVec3x3(m, bi0);
            double[] bisector2 = matDotVec3x3(m, bi2);
            negZ[2] = -lenSeg;

            for (int j = 0; j < TESS; j++) {
                double[] ep0Front = matDotVec2x3(xformArray[inext - 1], contour[j]);
                ep0Front[2] = 0.0;
                double[] ep2Front = matDotVec2x3(xformArray[inext - 1], contour[j]);
                ep2Front[2] = -lenSeg;
                frontLoop[j] = innerSect(origin, bisector0, ep0Front, ep2Front);

                double[] ep0Back = matDotVec2x3(xformArray[inext], contour[j]);
                ep0Back[2] = 0.0;
                double[] ep2Back = matDotVec2x3(xformArray[inext], contour[j]);
                ep2Back[2] = -lenSeg;
                backLoop[j] = innerSect(negZ, bisector2, ep0Back, ep2Back);
            }

            float[] colFront = colourArray[inext - 1];
            float[] colBack = colourArray[inext];
            float vFront = start + (inext - 1) * texSlice;
            float vBack = start + inext * texSlice;

            for (int k = 0; k < TESS; k++) {
                int kNext = (k + 1) % TESS;
                float u0 = (float) k / TESS;
                float u1 = (float) (k + 1) / TESS;
                vertex(consumer, mat, m, frontLoop[k], u0, vFront, colFront, light);
                vertex(consumer, mat, m, backLoop[k], u0, vBack, colBack, light);
                vertex(consumer, mat, m, backLoop[kNext], u1, vBack, colBack, light);
                vertex(consumer, mat, m, frontLoop[kNext], u1, vFront, colFront, light);
            }

            lenSeg = len;
            i = inext;
            inext = inextnext;
            bi0 = bi2;
            yup = vecReflect(yup, bi0);
        }
    }

    private static void vertex(VertexConsumer consumer, Matrix4f mat, double[][] m, double[] local, float u, float v, float[] c, int light) {
        double wx = m[0][0] * local[0] + m[1][0] * local[1] + m[2][0] * local[2] + m[3][0];
        double wy = m[0][1] * local[0] + m[1][1] * local[1] + m[2][1] * local[2] + m[3][1];
        double wz = m[0][2] * local[0] + m[1][2] * local[1] + m[2][2] * local[2] + m[3][2];
        consumer.addVertex(mat, (float) wx, (float) wy, (float) wz).setColor(c[0], c[1], c[2], c[3]).setUv(u, v);
    }

    private static double[] pickInitialUp(int npoints, double[][] pointArray) {
        double[] diff = vecDiff(pointArray[1], pointArray[0]);
        double len = vecLen(diff);
        if (len == 0.0) {
            for (int idx = 1; idx < npoints - 2; idx++) {
                diff = vecDiff(pointArray[idx + 1], pointArray[idx]);
                len = vecLen(diff);
                if (len != 0.0)
                    break;
            }
        }
        if (len == 0.0)
            return new double[]{0.0, 1.0, 0.0};
        double[] dirHat = vecScale(1.0 / len, diff);
        double[] up = (Math.abs(dirHat[0]) < 1.0E-6 && Math.abs(dirHat[2]) < 1.0E-6) ? new double[]{0.0, 0.0, 1.0} : new double[]{0.0, 1.0, 0.0};
        double[] perp = vecPerp(up, dirHat);
        if (vecLen(perp) == 0.0)
            return dirHat;
        return perp;
    }

    private static int findNonDegeneratePoint(int index, int npoints, double[] diffOut, double[][] pointArray) {
        int idx = index;
        double tlen;
        double[] tdiff;
        double[] sum;
        double slen;
        do {
            tdiff = vecDiff(pointArray[idx + 1], pointArray[idx]);
            diffOut[0] = tdiff[0];
            diffOut[1] = tdiff[1];
            diffOut[2] = tdiff[2];
            tlen = vecLen(diffOut);
            sum = vecSum(pointArray[idx + 1], pointArray[idx]);
            slen = vecLen(sum) * DEGENERATE_TOLERANCE;
            idx++;
        } while (tlen <= slen && idx < npoints - 1);
        return idx;
    }

    private static double[] innerSect(double[] p, double[] n, double[] v1, double[] v2) {
        double deno = (v1[0] - v2[0]) * n[0] + (v1[1] - v2[1]) * n[1] + (v1[2] - v2[2]) * n[2];
        double[] sect = new double[3];
        if (deno == 0.0) {
            return new double[]{v1[0], v1[1], v1[2]};
        }
        double numer = (p[0] - v2[0]) * n[0] + (p[1] - v2[1]) * n[1] + (p[2] - v2[2]) * n[2];
        double t = numer / deno;
        double omt = 1.0 - t;
        sect[0] = t * v1[0] + omt * v2[0];
        sect[1] = t * v1[1] + omt * v2[1];
        sect[2] = t * v1[2] + omt * v2[2];
        return sect;
    }

    private static double[] bisectingPlane(double[] v1, double[] v2, double[] v3) {
        double[] v4 = vecDiff(v2, v1);
        double[] v5 = vecDiff(v3, v2);
        double len1 = vecLen(v4);
        double len2 = vecLen(v5);
        if (len1 <= DEGENERATE_TOLERANCE * len2) {
            if (len2 == 0.0)
                return new double[]{0.0, 0.0, 0.0};
            return vecScale(1.0 / len2, v5);
        }
        if (len2 <= DEGENERATE_TOLERANCE * len1) {
            return vecScale(1.0 / len1, v4);
        }
        v4 = vecScale(1.0 / len1, v4);
        v5 = vecScale(1.0 / len2, v5);
        double dot = vecDot(v5, v4);
        if (dot >= 0.999998 || dot <= -0.999998) {
            return new double[]{v4[0], v4[1], v4[2]};
        }
        double[] n = new double[3];
        n[0] = dot * (v5[0] + v4[0]) - v5[0] - v4[0];
        n[1] = dot * (v5[1] + v4[1]) - v5[1] - v4[1];
        n[2] = dot * (v5[2] + v4[2]) - v5[2] - v4[2];
        return vecNormalize(n);
    }

    private static double[][] uviewpoint(double[] v1, double[] v2, double[] up) {
        double[] vHat21 = vecDiff(v2, v1);
        double[][] rotMat = uviewDirection(vHat21, up);
        double[][] transMat = identity4x4();
        transMat[3][0] = v1[0];
        transMat[3][1] = v1[1];
        transMat[3][2] = v1[2];
        return matProduct4x4(rotMat, transMat);
    }

    private static double[][] uviewDirection(double[] v21, double[] up) {
        double[] vHat21 = new double[]{v21[0], v21[1], v21[2]};
        double len = vecLen(vHat21);
        double[][] amat;
        double[][] bmat;
        double[][] cmat;
        if (len != 0.0) {
            vHat21 = vecScale(1.0 / len, vHat21);
            double sine = Math.sqrt(1.0 - vHat21[2] * vHat21[2]);
            amat = rotYcs(-vHat21[2], -sine);
        } else {
            amat = identity4x4();
        }
        double[] vXy = new double[]{v21[0], v21[1], 0.0};
        len = vecLen(vXy);
        if (len != 0.0) {
            vXy = vecScale(1.0 / len, vXy);
            bmat = rotZcs(vXy[0], vXy[1]);
            cmat = matProduct4x4(amat, bmat);
        } else {
            cmat = copy4x4(amat);
        }
        double[] upProj = vecPerp(up, vHat21);
        len = vecLen(upProj);
        double[][] m;
        if (len != 0.0) {
            upProj = vecScale(1.0 / len, upProj);
            double cosine = cmat[1][0] * upProj[0] + cmat[1][1] * upProj[1] + cmat[1][2] * upProj[2];
            double sine = cmat[0][0] * upProj[0] + cmat[0][1] * upProj[1] + cmat[0][2] * upProj[2];
            amat = rotZcs(cosine, -sine);
            m = matProduct4x4(amat, cmat);
        } else {
            m = copy4x4(cmat);
        }
        return m;
    }

    private static double[] matDotVec3x3(double[][] m, double[] v) {
        return new double[]{m[0][0] * v[0] + m[0][1] * v[1] + m[0][2] * v[2], m[1][0] * v[0] + m[1][1] * v[1] + m[1][2] * v[2], m[2][0] * v[0] + m[2][1] * v[1] + m[2][2] * v[2]};
    }

    private static double[] matDotVec2x3(double[][] m, double[] v) {
        return new double[]{m[0][0] * v[0] + m[0][1] * v[1] + m[0][2], m[1][0] * v[0] + m[1][1] * v[1] + m[1][2], 0.0};
    }

    private static double[] vecDiff(double[] a, double[] b) {
        return new double[]{a[0] - b[0], a[1] - b[1], a[2] - b[2]};
    }

    private static double[] vecSum(double[] a, double[] b) {
        return new double[]{a[0] + b[0], a[1] + b[1], a[2] + b[2]};
    }

    private static double[] vecScale(double s, double[] v) {
        return new double[]{s * v[0], s * v[1], s * v[2]};
    }

    private static double[] vecNormalize(double[] v) {
        double len = vecLen(v);
        if (len == 0.0)
            return new double[]{0.0, 0.0, 0.0};
        return vecScale(1.0 / len, v);
    }

    private static double[] vecReflect(double[] v, double[] n) {
        double dot = vecDot(v, n);
        return new double[]{v[0] - 2.0 * dot * n[0], v[1] - 2.0 * dot * n[1], v[2] - 2.0 * dot * n[2]};
    }

    private static double[] vecPerp(double[] v, double[] n) {
        double dot = vecDot(v, n);
        return new double[]{v[0] - dot * n[0], v[1] - dot * n[1], v[2] - dot * n[2]};
    }

    private static double vecLen(double[] v) {
        return Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
    }

    private static double vecDot(double[] a, double[] b) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }

    private static double[][] identity4x4() {
        double[][] m = new double[4][4];
        m[0][0] = m[1][1] = m[2][2] = m[3][3] = 1.0;
        return m;
    }

    private static double[][] copy4x4(double[][] a) {
        double[][] b = new double[4][4];
        for (int r = 0; r < 4; r++)
            System.arraycopy(a[r], 0, b[r], 0, 4);
        return b;
    }

    private static double[][] matProduct4x4(double[][] a, double[][] b) {
        double[][] c = new double[4][4];
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                c[row][col] = a[row][0] * b[0][col] + a[row][1] * b[1][col] + a[row][2] * b[2][col] + a[row][3] * b[3][col];
            }
        }
        return c;
    }

    private static double[][] rotYcs(double cosine, double sine) {
        double[][] m = identity4x4();
        m[0][0] = cosine;
        m[0][2] = -sine;
        m[2][0] = sine;
        m[2][2] = cosine;
        return m;
    }

    private static double[][] rotZcs(double cosine, double sine) {
        double[][] m = identity4x4();
        m[0][0] = cosine;
        m[0][1] = sine;
        m[1][0] = -sine;
        m[1][1] = cosine;
        return m;
    }
}
