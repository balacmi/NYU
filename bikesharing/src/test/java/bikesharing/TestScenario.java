package bikesharing;

import java.util.ArrayList;
import java.util.List;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkUtils;

class TestScenario {
	public TestScenario() {
		// We have a 1-dimensional world with 20x 500m long links

		Network network = NetworkUtils.createNetwork();

		List<Node> nodes = new ArrayList<>(21);

		for (int i = 0; i < 21; i++) {
			Node node = network.getFactory().createNode(Id.createNodeId("node" + i), new Coord(i * 500.0, 0.0));
			nodes.add(node);
		}
		
		List<Link> links = new ArrayList<>(20);

		for (int i = 0; i < 20; i++) {
			Link link = network.getFactory().createLink(Id.createLinkId("link" + i), nodes.get(i), nodes.get(i + 1));
			links.add(link);
		}
		
		nodes.forEach(network::addNode);
		links.forEach(network::addLink);
		
		
	}
}
