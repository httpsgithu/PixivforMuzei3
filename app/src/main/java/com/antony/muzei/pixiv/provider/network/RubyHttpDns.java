/*
 *     This file is part of PixivforMuzei3.
 *
 *     PixivforMuzei3 is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program  is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.antony.muzei.pixiv.provider.network;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import okhttp3.Dns;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.dnsoverhttps.DnsOverHttps;

/*
This class is called only when network bypass is enabled
 */
public class RubyHttpDns implements Dns {

    /**
     * 210.129.120.55 pixiv.net
     * 210.129.120.44 accounts.pixiv.net
     * 210.140.131.145 source.pixiv.net
     * 210.140.131.160 d.pixiv.org
     * 210.140.131.144 imagaz.pixiv.net
     * 210.129.120.55 www.pixiv.net
     */

    private static RubyHttpDns sHttpDns = null;
    private Dns dohDns;

    private static final String DOH_URL = "https://1.0.0.1/dns-query";
    private RubyHttpDns() {
        dohDns = new DnsOverHttps.Builder().client(new OkHttpClient())
                .url(HttpUrl.get(DOH_URL))
                .build();
    }


    /*private static final String[] addresses = {"123.207.56.160", "123.207.252.208", "202.141.162.123",
            "40.73.101.101", "123.207.5.191", "210.129.120.45"};*/

    public static RubyHttpDns getInstance() {
        if (sHttpDns == null) {
            sHttpDns = new RubyHttpDns();
        }
        return sHttpDns;
    }

    /*著作权归作者所有。
    商业转载请联系作者获得授权，非商业转载请注明出处。
    Mashiro
    链接：https://2heng.xin/2017/09/19/pixiv/
    来源：樱花庄的白猫
    #Pixiv Start
    210.129.120.49  pixiv.net
    210.129.120.49  www.pixiv.net
    210.140.92.134  i.pximg.net
    210.140.131.146 source.pixiv.net
    210.129.120.56  accounts.pixiv.net
    210.129.120.56  touch.pixiv.net
    210.140.131.147 imgaz.pixiv.net
    210.129.120.44  app-api.pixiv.net
    210.129.120.48  oauth.secure.pixiv.net
    210.129.120.41  dic.pixiv.net
    210.140.131.153 comic.pixiv.net
    210.129.120.43  factory.pixiv.net
    74.120.148.207  g-client-proxy.pixiv.net
    210.140.174.37  sketch.pixiv.net
    210.129.120.43  payment.pixiv.net
    210.129.120.41  sensei.pixiv.net
    210.140.131.144 novel.pixiv.net
    210.129.120.44  en-dic.pixiv.net
    210.140.131.145 i1.pixiv.net
    210.140.131.145 i2.pixiv.net
    210.140.131.145 i3.pixiv.net
    210.140.131.145 i4.pixiv.net
    210.140.131.159 d.pixiv.org
    210.140.92.141  s.pximg.net
    210.140.92.135  pixiv.pximg.net
    210.129.120.56  fanbox.pixiv.net
    #Pixiv End*/

    public List<InetAddress> lookup(String paramString)
            throws UnknownHostException {
        try {
            if (paramString.endsWith("pixiv.net")) {
                return dohDns.lookup("pixiv.net");
            } else {
                return dohDns.lookup(paramString);
            }
        } catch (Exception localException) {
            localException.printStackTrace();
        }
        return Dns.SYSTEM.lookup(paramString);
    }
    //private static final String[] addresses = {"210.140.131.147", "210.129.120.50", "210.140.92.135", "210.140.131.144", "210.129.120.46", "210.140.131.144"};
    //private static final String[] addresses = {"210.129.120.55", "210.129.120.44", "210.140.131.145", "210.140.131.160", "210.140.131.144"};
    //private static final String[] addresses = {"210.129.120.49", "210.140.131.146", "210.129.120.56", "210.129.120.44", "210.129.120.48"};
    //private static final String[] addresses = {"123.207.137.88", "202.141.162.123", "123.207.56.160", "115.159.220.214"};
}
