package app.snmp;

import org.snmp4j.CommunityTarget;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OctetString;

import app.snmp.client.SnmpClient;

public final class SnmpUtil {

	public static CommunityTarget createReadTarget(SnmpClient client) {
		CommunityTarget target = createTarget(client);
		target.setCommunity(new OctetString(client.getReadCommunity()));
		return target;
	}

	public static CommunityTarget createWriteTarget(SnmpClient client) {
		CommunityTarget target = createTarget(client);
		target.setCommunity(new OctetString(client.getWriteCommunity()));
		return target;
	}

	private static CommunityTarget createTarget(SnmpClient client) {
		Address address = GenericAddress.parse(client.getAddress() + "/" + client.getRequestPort());
		CommunityTarget target = new CommunityTarget();
		target.setCommunity(new OctetString(client.getReadCommunity()));
		target.setVersion(client.getSnmpVersion());
		target.setAddress(address);
		target.setRetries(client.getRetries());
		target.setTimeout(client.getTimeout());
		return target;
	}
}
