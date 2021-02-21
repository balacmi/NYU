package nyu.matsim.dockedservice.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.CommandLine;
import org.matsim.core.config.CommandLine.ConfigurationException;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.utils.collections.QuadTree;
import org.matsim.core.utils.collections.QuadTrees;

public class ConvertGBFS {
	static public void main(String[] args) throws ConfigurationException {
		CommandLine cmd = new CommandLine.Builder(args) //
				.requireOptions("network-path", "gbfs-station-path", "output-path", "network-modes") //
				.build();

		// Load network
		Network network = NetworkUtils.createNetwork();
		new MatsimNetworkReader(network).readFile(cmd.getOptionStrict("network-path"));

		// Process network modes
		Collection<String> networkModes = Arrays.asList(cmd.getOptionStrict("network-modes").split(",")).stream()
				.map(String::trim).collect(Collectors.toSet());
		Collection<Link> relevantLinks = new HashSet<>();

		for (Link link : network.getLinks().values()) {
			boolean isRelevant = false;

			for (String mode : link.getAllowedModes()) {
				isRelevant |= networkModes.contains(mode);
			}

			if (isRelevant) {
				relevantLinks.add(link);
			}
		}

		QuadTree<Link> spatialIndex = QuadTrees.createQuadTree(relevantLinks);
	}
}
