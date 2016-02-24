/**
 * 
 */

/**
 * @author keirmcluskey
 *
 */
public class Request {

	private String name, scenario;
	private long start, end, duration;
	private boolean success;
	
	public Request (String Name, String Scenario, long Start, long End, boolean Success) {
		this.name = Name;
		this.scenario = Scenario;
		this.start = Start;
		this.end = End;
		this.success = Success;
		this.duration = this.end - this.start;
	}
	
	public String getScenario() {
		return this.scenario;
	}
	
	public String getName() {
		return this.name;
	}
	
	public long getStart() {
		return this.start;
	}
	
	public long getEnd() {
		return this.end;
	}

	public long getDuration() {
		return this.duration;
	}
	
	public boolean getSuccess() {
		return this.success;
	}
}
