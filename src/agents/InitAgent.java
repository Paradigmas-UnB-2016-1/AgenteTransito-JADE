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
        Profile p = new ProfileImpl();     
        
        ContainerController cc = rt.createAgentContainer(p); 
        
        Object argsVia1[] = new Object[2,"aberta"];
        Object argsVia2[] = new Object[1,"fechada"];
        Object argsGuarda[] = new Object[];
        AgentController via1, via2, guarda;
        
        try {
            via1 = cc.createNewAgent("Avenida Rio", "agents.Via", argsVia1);
            via1.start();
            
            via2 = cc.createNewAgent("Avenida Minas", "agents.Via", argsVia2);
            via2.start();
            
            guarda = cc.createNewAgent("Guarda", "agents.Guarda", argsGuarda);
            guarda.start();
            
        } catch (StaleProxyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}

}