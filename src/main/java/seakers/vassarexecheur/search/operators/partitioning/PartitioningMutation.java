package seakers.vassarexecheur.search.operators.partitioning;

import org.moeaframework.core.Solution;
import seakers.architecture.Architecture;
import seakers.architecture.operators.IntegerUM;
import seakers.architecture.util.IntegerVariable;
import seakers.vassarexecheur.search.problems.partitioning.PartitioningArchitecture;
import seakers.vassarheur.BaseParams;

import java.util.*;

public class PartitioningMutation extends IntegerUM {

    protected double probability;
    protected BaseParams params;

    public PartitioningMutation(double probability, BaseParams params){
        super(probability);
        this.probability = probability;
        this.params = params;
    }

    @Override
    public Solution[] evolve(Solution[] parents) {
        Solution[] out = super.evolve(parents);
        out[0] = repair(out[0]);
        return out;
    }

    public int[] repair(int[] inputs){

        int[] partitioning = Arrays.copyOfRange(inputs, 0, params.getNumInstr());
        int[] assigning = Arrays.copyOfRange(inputs, params.getNumInstr(), 2 * params.getNumInstr());

        Architecture newArch = new PartitioningArchitecture(params.getNumInstr(), params.getNumOrbits(), 2, params);
        int[] newPartitioning = new int[partitioning.length];
        int[] newAssigning = new int[assigning.length];

        HashMap<Integer, Integer> satID2satIndex = new HashMap<>();
        HashMap<Integer, Integer> satIndex2Orbit = new HashMap<>();
        int satIndex = 0;
        for(int i = 0; i < partitioning.length; i++){
            int satID = partitioning[i];
            if(!satID2satIndex.containsKey(satID)){
                satID2satIndex.put(satID, satIndex);
                satIndex2Orbit.put(satIndex, assigning[satID]);
                satIndex++;
            }

            newPartitioning[i] = satID2satIndex.get(satID);
        }

        Arrays.sort(newPartitioning);

        for(int i = 0; i < assigning.length;i ++){

            if(satIndex2Orbit.containsKey(i)){
                int orb = satIndex2Orbit.get(i);
                if(orb == -1){
                    Set<Integer> orbitsUsed = new HashSet<>(satIndex2Orbit.values());
                    orbitsUsed.remove(-1);

                    ArrayList<Integer> orbitOptions;

                    if(orbitsUsed.size() == params.getNumOrbits()){
                        orbitOptions = new ArrayList<>(orbitsUsed);

                    }else{
                        orbitOptions = new ArrayList<>();
                        for(int j = 0; j < params.getNumOrbits(); j++){
                            if(!orbitsUsed.contains(j)){
                                orbitOptions.add(j);
                            }
                        }
                    }

                    Collections.shuffle(orbitOptions);
                    orb = orbitOptions.get(0);
                    satIndex2Orbit.put(i, orb);
                }
                newAssigning[i] = orb;

            }else{
                newAssigning[i] = -1;
            }
        }

        int[] newIntVars = new int[partitioning.length + assigning.length];
        for(int i = 0; i < partitioning.length;i ++){
            newIntVars[i] = newPartitioning[i];
        }
        for(int i = 0; i < assigning.length;i ++){
            newIntVars[i + newPartitioning.length] = newAssigning[i];
        }
        return newIntVars;
    }

    private Solution repair(Solution solution){
        Architecture arch = (PartitioningArchitecture) solution;
        int[] intVars = getIntVariables(arch);

        int[] newIntVars = repair(intVars);

        Architecture newArch = new PartitioningArchitecture(params.getNumInstr(), params.getNumOrbits(), 2, params);
        setIntVariables(newArch, newIntVars);
        return newArch;
    }

    private int[] getIntVariables(Architecture arch){
        int[] out = new int[arch.getNumberOfVariables()];
        for(int i = 0; i < out.length; i++){
            out[i] = ((IntegerVariable) arch.getVariable(i)).getValue();
        }
        return out;
    }

    private void setIntVariables(Architecture arch, int[] values){
        int[] out = new int[arch.getNumberOfVariables()];
        for(int i = 0; i < out.length; i++){
            ((IntegerVariable) arch.getVariable(i)).setValue(values[i]);
        }
    }
}
