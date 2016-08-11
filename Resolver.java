import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;


/**
 * In this class 2 approaches of the subset sum problem implemented:
 * naive with complexity O(N*2^N) and dynamic-programming base with pseudo-polynomial complexity
 * (but need more memory)
 **/

public class Main {

    // Naive threshold
    // If N < THRESHOLD - naive approach is better than dynamic-base
    public static final int NAIVE_THRESHOLD = 20;

    /**
     * Naive algorithm: all subsets, complexity: O(N*2^N)
     * @param ints
     * @return
     */
    public static boolean naive(List<Integer> ints){

        // optimisation: long value as index. Zeros by default.
        long idx;

        int sum;
        for(idx = 1; idx < Math.pow(2, ints.size()); idx ++){
            sum = 0;
            for(int i=0; i< ints.size(); i++) {
                if ((idx & (1L << i)) != 0) sum += ints.get(i);
            }
            if(sum==0) return true;
        }
        return false;
    }




    /**
     * Dynamic programming approach. Complexity: O(N*C), where C depends on input data (ints).
     * Based on wikipedia article: https://en.wikipedia.org/wiki/Subset_sum_problem
     * @param ints
     * @return
     */
    public static boolean dynamic(List<Integer> ints){

        // Another hack. If list size is less than NAIVE_THRESHOLD - better to use a naive
        // function
        if (ints.size() < NAIVE_THRESHOLD) return naive(ints);

        int s = 0;
        // Maxumum negative and positive sum's for boolean matrix range
        int A = ints.stream().mapToInt(value -> value<0?value:0).sum();
        int B = ints.stream().mapToInt(value -> value>0?value:0).sum();

        if(s < A || s > B) return false;

        // Boolean matrix
        boolean qs[][] = new boolean[ints.size()][B - A + 1];
        setElement(qs,0,ints.get(0).intValue(),A,true);

        for(int i=1;i < ints.size(); i++){
            for(int j=A; j< B+1; j++){
                if (        getElement(qs,i-1,j,A) ||
                        ints.get(i).intValue()==j ||
                        getElement(qs, i-1, j-ints.get(i).intValue(),A)
                        ) qs[i][j-A] = true;
                // critical optimization - IF we can hope that in the beginning of the data set we have a good sum
                // and we do not need to print a subset result.
                // this is a durty hack
                if (j == s && qs[i][j-A]) return true;

            }
        }

        // find a solution
        List<Integer> solution = new ArrayList<>();
        for(int i=ints.size()-1; i >= 0; i--){
            if(!getElement(qs,i-1,s,A) && getElement(qs,i,s,A)){

                // If you need to print a solution subset:
                s -= ints.get(i).intValue();
                solution.add(ints.get(i));
                // if not:
                //return true;
            }
        }

        return solution.size()>0;
    }

    /**
     * Optimized dynamic programming approach.
     *
     * @param ints
     * @return
     */
    public static boolean dynamicOpt(List<Integer> ints){

        // Another hack. If list size is less than NAIVE_THRESHOLD - better to use a naive
        // function
        if (ints.size() < NAIVE_THRESHOLD) return naive(ints);

        int s = 0;
        // Maxumum negative and positive sum's for boolean matrix range
        int A = ints.stream().mapToInt(value -> value<0?value:0).sum();
        int B = ints.stream().mapToInt(value -> value>0?value:0).sum();

        if(s < A || s > B) return false;

        // Boolean matrix
        // Memory usage optimized using following fact:
        // on the each iteration we need only current and previous case
        int dpSize = B - A + 1;
        boolean qsPrev[] = new boolean[dpSize];
        boolean qsCurr[] = new boolean[dpSize];

        int tmpPrevIdx;
        boolean tmpPrevVal;
        qsPrev[ints.get(0).intValue() - A] = true;

        for(int i=1;i < ints.size(); i++){
            for(int j=A; j< B+1; j++){
                tmpPrevIdx = j-ints.get(i).intValue() - A;
                if(tmpPrevIdx < 0 || tmpPrevIdx >= dpSize) tmpPrevVal = false;
                else tmpPrevVal = qsPrev[tmpPrevIdx];

                if (    qsPrev[j-A] || ints.get(i).intValue()==j || tmpPrevVal
                        ) qsCurr[j-A] = true;
                if (j == s && qsCurr[j-A]) return true;
            }
            qsPrev = qsCurr;
            qsCurr = new boolean[dpSize];
            //qsPrev = qsCurr.clone();
            //Arrays.fill(qsCurr,false);
        }
        return false;
    }



    /**
     * Optimized dynamic programming approach with 1vector instead of NxSums matrix
     * I used a fact, that each row contains info about previous and we can be even more memory effective
     * (and avoid memory allocations for new vectors for swapping)
     *
     * @param ints
     * @return
     */
    public static boolean dynamicOptOneVector(List<Integer> ints){

        // Another hack. If list size is less than NAIVE_THRESHOLD - better to use a naive
        // function
        if (ints.size() < NAIVE_THRESHOLD) return naive(ints);

        int s = 0;
        // Maxumum negative and positive sum's for boolean matrix range
        int A = ints.stream().mapToInt(value -> value<0?value:0).sum();
        int B = ints.stream().mapToInt(value -> value>0?value:0).sum();

        if(s < A || s > B) return false;

        // Boolean matrix
        // Memory usage optimized using following fact:
        // on the each iteration we need only current and previous case
        int dpSize = B - A + 1;
        int numPossibleSums = 1;
        boolean qsCurr[] = new boolean[dpSize];
        qsCurr[ints.get(0).intValue() - A] = true;
        for(int i=1;i < ints.size(); i++){
            qsCurr[ints.get(i).intValue() - A] = true;
            for(int j=A; (j< B+1) && numPossibleSums < (i + 1); j++){
                try {
                    qsCurr[j-A] |= qsCurr[j - ints.get(i).intValue() - A];
                } catch (RuntimeException ex){}
                if (j == s && qsCurr[j-A]) return true;
            }
        }
        return false;
    }



    public static void setElement(boolean [][]tdp, int i, int j, int negSum, boolean val){
        tdp[i][j - negSum] = val;
    }

    public static boolean getElement(boolean [][]tdp, int i, int j, int negSum){
        try {
            return tdp[i][j - negSum];
        } catch (ArrayIndexOutOfBoundsException ex){
            // in case of... :-)
            return false;
        }
    }

    public static boolean getElement1(boolean []tdp, int j, int negSum){
        try {
            return tdp[j - negSum];
        } catch (ArrayIndexOutOfBoundsException ex){
            // in case of... :-)
            return false;
        }
    }





    /**
     * Test 2 approaches: naive with complexity: O(N*2^N) and dynamic programming approach with pseudo-polinomial time: O(N*C),
     * where C is a constant, depends on data (positive SUM - negative SUM).
     * Naive approach works with a tiny optimization: long as index.
     * dynamic approach works better with N>20, but use much more memory for dp-matrix. Could be optimized.
     * @param args
     */
    public static void main(String[] args) {
        Random rand = new Random();
        rand.setSeed(123);
        List<Integer> ints = new ArrayList<>();
        // [-1, 10, 5, 3, 2, 1]
        // [-2, -1, 5, 5, -1, 10, 4]

        ints.addAll(rand.ints(2000,-65000,65000).boxed().collect(Collectors.toList()));
//

//        ints.add(4);
//
//        ints.add(2);
//        ints.add(2);
//        ints.add(-7);
//        ints.add(2);
//        ints.add(2);
//        ints.add(2);
//        ints.add(1);

        //List<Integer> ints = Arrays.asList(1,2,3, -3);
        //System.out.println(Arrays.toString(ints.toArray()));

        boolean res=false;
        long tStart;


        System.out.println("Dynamic - OPTIMIZED. Use it for thousands and more elements! ..");
        tStart = System.currentTimeMillis();
        res = dynamicOpt(ints);
        //res = dynamicOptLongIDX(ints);
        System.out.println("result: " + res);
        System.out.println("time: " + (System.currentTimeMillis() - tStart));


        System.out.println("Dynamic - One Vector OPTIMIZED. Use it for thousands and more elements! ..");
        tStart = System.currentTimeMillis();
        res = dynamicOptOneVector(ints);
        //res = dynamicOptLongIDX(ints);
        System.out.println("result: " + res);
        System.out.println("time: " + (System.currentTimeMillis() - tStart));


    }


}
