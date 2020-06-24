/*------------------------------------------------------------------------------
 - Copyright (c) 2020. This code follow the GPL v3 license scheme.
 -----------------------------------------------------------------------------*/

package it.aequinoxio;import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.UnknownHostException;

/**
 * Classe per verificare se un IP appartiene ad una subnet
 */
public class CheckIpInCidrSubnet {
    /**
     * Verifica se l'ip appartiene alla subnet indicata rispetto alla netmask
     * @param ip Indirizzo da verificare in formato binario
     * @param subnet Subnet in formato binario
     * @param netmask Maschera di bit
     * @return True se l'ip appartiene alla subnet, false altrimenti
     */
    static boolean checkIpInCidr(long ip, long subnet, long netmask){
        return  (ip & netmask) == (subnet & netmask);
    }

    /**
     * Verifica se l'ip appartiene alla subnet indicata rispetto alla subnet in formato Cidr
     * @param ip Indirizzo da verificare (es. 192.168.0.1)
     * @param subnetCidr Subnet e netmask in formato Cidr (es. 192.168.0.0/16)
     * @return True se l'ip appartiene alla subnet, false altrimenti
     * @throws UnknownHostException Nel caso la subnet non sia in formato CIDR
     */
    static boolean checkIpInCidr(String ip, String subnetCidr) throws UnknownHostException {
        String[] s = subnetCidr.split("/");
        if (s.length != 2){
            throw new UnknownHostException("Non in formato CIDR");
        }

        long netbit = new BigInteger(1, Inet4Address.getByName(s[1]).getAddress()).longValue();

        return (checkIpInCidr(
                new BigInteger(1,Inet4Address.getByName(ip).getAddress()).longValue(),
                new BigInteger(1,Inet4Address.getByName(s[0]).getAddress()).longValue(),
                (-1L<<(32-netbit))&0xFFFFFFFFL
        ));

    }
}
