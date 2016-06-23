package agents;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;


public class InitAgent extends Agent
{

	private static final long serialVersionUID = 2963709068114684396L;

	protected void setup()
    {
		
        Runtime rt = Runtime.instance(); 
        Profile profile = new ProfileImpl();     
        
        ContainerController container = rt.createAgentContainer(profile); 
        
        Object argsVia1[] = new Object[2];
        argsVia1[0] = "2";
        argsVia1[1] = "aberta";
        argsVia1[2] = "1000"; //Tempo saída em ms
        argsVia1[3] = "3000"; //Tempo chegada em ms
        Object argsVia2[] = new Object[2];
        argsVia2[0] = "1";
        argsVia2[1] = "fechada";
        argsVia2[2] = "1000"; //Tempo saída em ms
        argsVia2[3] = "3000"; //Tempo chegada em ms
        Object argsGuarda[] = new Object[0];
        AgentController via1, via2, guarda;
        
        try {
            via1 = container.createNewAgent("Avenida Rio", "agents.Via", argsVia1);
            via1.start();
            
            Thread.sleep(100);
            
            via2 = container.createNewAgent("Avenida Minas", "agents.Via", argsVia2);
            via2.start();
            
            Thread.sleep(100);
            
            guarda = container.createNewAgent("Guarda", "agents.Guarda", argsGuarda);
            guarda.start();
            
        } catch (StaleProxyException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}