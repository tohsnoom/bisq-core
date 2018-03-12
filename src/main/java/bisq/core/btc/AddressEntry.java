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

package bisq.core.btc;

import bisq.core.app.BisqEnvironment;

import bisq.common.proto.ProtoUtil;
import bisq.common.proto.persistable.PersistablePayload;
import bisq.common.util.Utilities;

import com.google.protobuf.ByteString;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.crypto.DeterministicKey;

import java.util.Optional;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;



import bisq.generated.protobuffer.PB;

/**
 * Every trade use a addressEntry with a dedicated address for all transactions related to the trade.
 * That way we have a kind of separated trade wallet, isolated from other transactions and avoiding coin merge.
 * If we would not avoid coin merge the user would lose privacy between trades.
 */
@EqualsAndHashCode
@Slf4j
public final class AddressEntry implements PersistablePayload {
    public enum Context {
        ARBITRATOR,
        AVAILABLE,
        OFFER_FUNDING,
        RESERVED_FOR_TRADE,
        MULTI_SIG,
        TRADE_PAYOUT
    }

    // keyPair can be null in case the object is created from deserialization as it is transient.
    // It will be restored when the wallet is ready at setDeterministicKey
    // So after startup it never must be null

    @Nullable
    @Getter
    private final String offerId;
    @Getter
    private final Context context;
    @Getter
    private final byte[] pubKey;
    @Getter
    private final byte[] pubKeyHash;

    private long coinLockedInMultiSig;

    @Nullable
    transient private DeterministicKey keyPair;
    @Nullable
    transient private Address address;
    @Nullable
    transient private String addressString;


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Constructor, initialization
    ///////////////////////////////////////////////////////////////////////////////////////////

    public AddressEntry(DeterministicKey keyPair, Context context) {
        this(keyPair, context, null);
    }

    public AddressEntry(@NotNull DeterministicKey keyPair,
                        Context context,
                        @Nullable String offerId) {
        this.keyPair = keyPair;
        this.context = context;
        this.offerId = offerId;
        pubKey = keyPair.getPubKey();
        pubKeyHash = keyPair.getPubKeyHash();
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // PROTO BUFFER
    ///////////////////////////////////////////////////////////////////////////////////////////

    private AddressEntry(byte[] pubKey,
                         byte[] pubKeyHash,
                         Context context,
                         @Nullable String offerId,
                         Coin coinLockedInMultiSig) {
        this.pubKey = pubKey;
        this.pubKeyHash = pubKeyHash;
        this.context = context;
        this.offerId = offerId;
        this.coinLockedInMultiSig = coinLockedInMultiSig.value;
    }

    public static AddressEntry fromProto(PB.AddressEntry proto) {
        return new AddressEntry(proto.getPubKey().toByteArray(),
                proto.getPubKeyHash().toByteArray(),
                ProtoUtil.enumFromProto(AddressEntry.Context.class, proto.getContext().name()),
                ProtoUtil.stringOrNullFromProto(proto.getOfferId()),
                Coin.valueOf(proto.getCoinLockedInMultiSig()));
    }

    @Override
    public PB.AddressEntry toProtoMessage() {
        PB.AddressEntry.Builder builder = PB.AddressEntry.newBuilder()
                .setPubKey(ByteString.copyFrom(pubKey))
                .setPubKeyHash(ByteString.copyFrom(pubKeyHash))
                .setContext(PB.AddressEntry.Context.valueOf(context.name()))
                .setCoinLockedInMultiSig(coinLockedInMultiSig);
        Optional.ofNullable(offerId).ifPresent(builder::setOfferId);
        return builder.build();
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // API
    ///////////////////////////////////////////////////////////////////////////////////////////

    // Set after wallet is ready
    public void setDeterministicKey(DeterministicKey deterministicKey) {
        this.keyPair = deterministicKey;
    }

    // getKeyPair must not be called before wallet is ready (in case we get the object recreated from disk deserialization)
    // If the object is created at runtime it must be always constructed after wallet is ready.
    @NotNull
    public DeterministicKey getKeyPair() {
        checkNotNull(keyPair, "keyPair must not be null. If we got the addressEntry created from PB we need to have " +
                "setDeterministicKey got called before any access with getKeyPair().");
        return keyPair;
    }

    public void setCoinLockedInMultiSig(@NotNull Coin coinLockedInMultiSig) {
        this.coinLockedInMultiSig = coinLockedInMultiSig.value;
    }

    // For display we usually only display the first 8 characters.
    @Nullable
    public String getShortOfferId() {
        return offerId != null ? Utilities.getShortId(offerId) : null;
    }

    @Nullable
    public String getAddressString() {
        if (addressString == null && getAddress() != null)
            addressString = getAddress().toString();
        return addressString;
    }

    @Nullable
    public Address getAddress() {
        if (address == null && keyPair != null)
            address = keyPair.toAddress(BisqEnvironment.getParameters());
        return address;
    }

    public boolean isOpenOffer() {
        return context == Context.OFFER_FUNDING || context == Context.RESERVED_FOR_TRADE;
    }

    public boolean isTrade() {
        return context == Context.MULTI_SIG || context == Context.TRADE_PAYOUT;
    }

    public boolean isTradable() {
        return isOpenOffer() || isTrade();
    }

    public Coin getCoinLockedInMultiSig() {
        return Coin.valueOf(coinLockedInMultiSig);
    }

    @Override
    public String toString() {
        return "AddressEntry{" +
                "offerId='" + getOfferId() + '\'' +
                ", context=" + context +
                ", address=" + getAddressString() +
                '}';
    }
}
