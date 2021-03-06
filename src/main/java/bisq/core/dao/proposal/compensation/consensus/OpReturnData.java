/*
 * This file is part of Bisq.
 *
 * bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.core.dao.proposal.compensation.consensus;

import bisq.core.dao.OpReturnTypes;

import bisq.common.app.Version;
import bisq.common.crypto.Hash;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OpReturnData {

    public static byte[] getBytes(String input) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] dataAndSigAsBytes = input.getBytes();
            outputStream.write(OpReturnTypes.COMPENSATION_REQUEST);
            outputStream.write(Version.COMPENSATION_REQUEST_VERSION);
            outputStream.write(Hash.getSha256Ripemd160hash(dataAndSigAsBytes));
            return outputStream.toByteArray();
        } catch (IOException e) {
            // Not expected to happen ever
            e.printStackTrace();
            log.error(e.toString());
            return new byte[0];
        }
    }
}
