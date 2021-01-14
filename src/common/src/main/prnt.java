package common.src.main;

import org.jspace.FormalField;
import org.jspace.SequentialSpace;
import org.jspace.Space;
import org.jspace.SpaceRepository;

public class prnt {
    public static void main(String[] args) {
        try {

            Space space = new SequentialSpace();
            SpaceRepository spaceRepository = new SpaceRepository();
            spaceRepository.add("space", space);
            spaceRepository.addGate("tcp://localhost:5000/?keep");

            while (true){

                String out = (String) space.get(new FormalField(String.class))[0];
                System.out.println(out);

            }


        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
