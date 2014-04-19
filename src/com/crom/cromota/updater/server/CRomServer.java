/*
 * Copyright 2014 C-RoM Project
 * Parthipan Ramesh
 *
 * C-RoM OTA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * C-RoM OTA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with C-RoM OTA.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.crom.cromota.updater.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.crom.cromota.Version;
import com.crom.cromota.updater.Server;
import com.crom.cromota.updater.UpdatePackage;
import com.crom.cromota.updater.Updater.PackageInfo;

public class CRomServer implements Server {

    private static final String URL = "http://get.c-rom.org/update.php?device=%s";

    private String mDevice = null;
    private String mError = null;
    private Version mVersion;

    @Override
    public String getUrl(String device, Version version) {
        mDevice = device;
        mVersion = version;
        return String.format(URL, new Object[] { device });
    }

    @Override
    public List<PackageInfo> createPackageInfoList(JSONObject response) throws Exception {
        mError = null;
        List<PackageInfo> list = new ArrayList<PackageInfo>();
        mError = response.optString("error");
        if (mError == null || mError.isEmpty()) {
            JSONArray updates = response.getJSONArray("updates");
            for (int i = updates.length() - 1; i >= 0; i--) {
                JSONObject file = updates.getJSONObject(i);
                String filename = file.optString("name");
                Version version = new Version(filename, false);
                if (Version.compare(mVersion, version) < 0) {
                    list.add(new UpdatePackage(mDevice, filename, version, file.getString("size"),
                            file.getString("url"), file.getString("md5"), false));
                }
            }
        }
        Collections.sort(list, new Comparator<PackageInfo>() {

            @Override
            public int compare(PackageInfo lhs, PackageInfo rhs) {
                return Version.compare(lhs.getVersion(), rhs.getVersion());
            }

        });
        Collections.reverse(list);
        return list;
    }

    @Override
    public String getError() {
        return mError;
    }

}
