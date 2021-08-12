/*===============================================================================
 * Copyright (c) 2010-2012 University of Massachusetts.  All Rights Reserved.
 *
 * Use of the RankLib package is subject to the terms of the software license set
 * forth in the LICENSE file included with this software, and also available at
 * http://people.cs.umass.edu/~vdang/ranklib_license.html
 *===============================================================================
 */

package ciir.umass.edu.learning;

import ciir.umass.edu.utilities.RankLibError;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Implements a sparse data point using a compressed sparse row data structure
 * @author Siddhartha Bagaria
 */
public class SparseDataPoint extends DataPoint {

    // Access pattern of the feature values
    private enum accessPattern {SEQUENTIAL, RANDOM};
    private static accessPattern searchPattern = accessPattern.RANDOM;

    // Profiling variables
    // private static int numCalls = 0;
    // private static float avgTime = 0;

    // The feature ids for known values
    int fIds[];

    // The feature values for corresponding Ids
    //float fVals[]; //moved to the parent class

    // Internal search optimizers. Currently unused.
    int lastMinId = -1;
    int lastMinPos = -1;

    public SparseDataPoint(String text) {
        super(text);
    }

    public SparseDataPoint(SparseDataPoint dp)
    {
        label = dp.label;
        id = dp.id;
        description = dp.description;
        cached = dp.cached;
        fIds = new int[dp.fIds.length];
        fVals = new float[dp.fVals.length];
        System.arraycopy(dp.fIds, 0, fIds, 0, dp.fIds.length);
        System.arraycopy(dp.fVals, 0, fVals, 0, dp.fVals.length);
    }

    /**
     * <p>Allows the direct allocation of a SparseDataPoint instead of having it parse text.</p>
     * <p>The fVals array represents the sparse feature values. Only non-null values must be specified.</p>
     * <p>The fIds array has equal length to fVals and specifies for each position the feature ID that this position
     * in both array stands for. Thus, if fIds[1] == '5' then fVals[5] is the value of this data point for feature
     * number 5. The values in fIds must be sorted in an ascending manner.</p>
     *
     * @param fVals          The feature values, starting at index 0.
     * @param fIds           The feature IDs, starting at index 0.
     * @param queryId        The ID of the query this datapoint is associated with.
     * @param relevanceLabel The relevance label of this datapoint with respect to the query.
     */
    public SparseDataPoint(float[] fVals, int[] fIds, int numKnownFeatures, String queryId, float relevanceLabel)
    {
        this.fVals = fVals;
        this.fIds = fIds;
        this.knownFeatures = numKnownFeatures;
        this.id = queryId;
        this.label = relevanceLabel;
    }

    private int locate(int fid) {
        if (searchPattern == accessPattern.SEQUENTIAL)
        {
            if (lastMinId > fid)
            {
                lastMinId = -1;
                lastMinPos = -1;
            }
            while (lastMinPos < knownFeatures && lastMinId < fid)
                lastMinId = fIds[++lastMinPos];
            if (lastMinId == fid)
                return lastMinPos;
        }
        else if (searchPattern == accessPattern.RANDOM)
        {
            int pos = Arrays.binarySearch(fIds, fid);
            if (pos >= 0)
                return pos;
        }
        else
            System.err.println("Invalid search pattern specified for sparse data points.");

        return -1;
    }

    public boolean hasFeature(int fid) {
        return locate(fid) != -1;
    }

    @Override
    public float getFeatureValue(int fid)
    {
        //long time = System.nanoTime();
        if(fid <= 0 || fid > knownFeatures)
        {
            if (missingZero) return 0f;
            throw RankLibError.create("Error in SparseDataPoint::getFeatureValue(): requesting unspecified feature, fid=" + fid);
        }
        int pos = locate(fid);
        //long completedIn = System.nanoTime() - time;
        //avgTime = (avgTime*numCalls + completedIn)/(++numCalls);
        //System.out.println("getFeatureValue average time: "+avgTime);
        if(pos >= 0)
            return fVals[pos];

        return 0; // Should ideally be returning unknown?
    }

    @Override
    public void setFeatureValue(int fid, float fval)
    {
        if(fid <= 0 || fid > knownFeatures)
        {
            throw RankLibError.create("Error in SparseDataPoint::setFeatureValue(): feature (id=" + fid + ") out of range.");
        }
        int pos = locate(fid);
        if(pos >= 0)
            fVals[pos] = fval;
        else
        {
            System.err.println("Error in SparseDataPoint::setFeatureValue(): feature (id=" + fid + ") not found.");
            System.exit(1);
        }
    }

    @Override
    public void setFeatureVector(float[] dfVals)
    {
        // Changed by EF in the JULIE Lab to avoid the usage of the static 'featureCount' field
        int numPresentFeatures = (int) IntStream.range(0, dfVals.length).mapToDouble(i -> dfVals[i]).filter(val -> !Double.isNaN(val) && val != 0).count();
        fIds = new int[numPresentFeatures];
        fVals = new float[numPresentFeatures];
        int pos = 0;
        for (int i = 1; i < dfVals.length; i++) {
            if (!isUnknown(dfVals[i])) {
                fIds[pos] = i;
                fVals[pos] = dfVals[i];
                pos++;
            }
        }
        assert (pos == numPresentFeatures);
    }

    @Override
    public float[] getFeatureVector() {
        // Changed by EF in the JULIE Lab because position 0 is unused (note how 'setFeatureVector' starts with fId 1)
        float[] dfVals = new float[knownFeatures + 1];
        Arrays.fill(dfVals, UNKNOWN);
        for (int i = 0; i < fIds.length; i++)
            dfVals[fIds[i]] = fVals[i];
        return dfVals;
    }
}