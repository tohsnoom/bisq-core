/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.core.arbitration.messages;

import bisq.core.arbitration.Dispute;
import bisq.core.proto.CoreProtoResolver;
import io.bisq.common.app.Version;
import io.bisq.generated.protobuffer.PB;
import io.bisq.network.p2p.NodeAddress;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public final class PeerOpenedDisputeMessage extends DisputeMessage {
    private final Dispute dispute;
    private final NodeAddress senderNodeAddress;

    public PeerOpenedDisputeMessage(Dispute dispute,
                                    NodeAddress senderNodeAddress,
                                    String uid) {
        this(dispute,
                senderNodeAddress,
                uid,
                Version.getP2PMessageVersion());
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // PROTO BUFFER
    ///////////////////////////////////////////////////////////////////////////////////////////

    private PeerOpenedDisputeMessage(Dispute dispute,
                                     NodeAddress senderNodeAddress,
                                     String uid,
                                     int messageVersion) {
        super(messageVersion, uid);
        this.dispute = dispute;
        this.senderNodeAddress = senderNodeAddress;
    }

    @Override
    public PB.NetworkEnvelope toProtoNetworkEnvelope() {
        return getNetworkEnvelopeBuilder()
                .setPeerOpenedDisputeMessage(PB.PeerOpenedDisputeMessage.newBuilder()
                        .setUid(uid)
                        .setDispute(dispute.toProtoMessage())
                        .setSenderNodeAddress(senderNodeAddress.toProtoMessage()))
                .build();
    }

    public static PeerOpenedDisputeMessage fromProto(PB.PeerOpenedDisputeMessage proto, CoreProtoResolver coreProtoResolver, int messageVersion) {
        return new PeerOpenedDisputeMessage(Dispute.fromProto(proto.getDispute(), coreProtoResolver),
                NodeAddress.fromProto(proto.getSenderNodeAddress()),
                proto.getUid(),
                messageVersion);
    }
}