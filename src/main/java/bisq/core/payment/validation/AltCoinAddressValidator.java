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

package bisq.core.payment.validation;

import bisq.core.app.BisqEnvironment;
import bisq.core.locale.Res;
import bisq.core.payment.validation.altcoins.ByteballAddressValidator;
import bisq.core.payment.validation.altcoins.NxtReedSolomonValidator;
import bisq.core.payment.validation.altcoins.OctocoinAddressValidator;
import bisq.core.payment.validation.altcoins.PNCAddressValidator;
import bisq.core.payment.validation.altcoins.WMCCAddressValidator;
import bisq.core.payment.validation.altcoins.XCNAddressValidator;
import bisq.core.payment.validation.altcoins.YTNAddressValidator;
import bisq.core.payment.validation.altcoins.KOTOAddressValidator;
import bisq.core.payment.validation.params.ACHParams;
import bisq.core.payment.validation.params.AlcParams;
import bisq.core.payment.validation.params.CageParams;
import bisq.core.payment.validation.params.CreaParams;
import bisq.core.payment.validation.params.IOPParams;
import bisq.core.payment.validation.params.ODNParams;
import bisq.core.payment.validation.params.OctocoinParams;
import bisq.core.payment.validation.params.OnionParams;
import bisq.core.payment.validation.params.PARTParams;
import bisq.core.payment.validation.params.PhoreParams;
import bisq.core.payment.validation.params.PNCParams;
import bisq.core.payment.validation.params.PivxParams;
import bisq.core.payment.validation.params.SpeedCashParams;
import bisq.core.payment.validation.params.StrayaParams;
import bisq.core.payment.validation.params.TerracoinParams;
import bisq.core.payment.validation.params.WACoinsParams;
import bisq.core.payment.validation.params.WMCCParams;
import bisq.core.payment.validation.params.XspecParams;
import bisq.core.payment.validation.params.btc.BTGParams;
import bisq.core.payment.validation.params.btc.BtcMainNetParamsForValidation;
import bisq.core.util.validation.InputValidator;

import org.libdohj.params.DashMainNetParams;
import org.libdohj.params.DashRegTestParams;
import org.libdohj.params.DashTestNet3Params;
import org.libdohj.params.DogecoinMainNetParams;
import org.libdohj.params.LitecoinMainNetParams;
import org.libdohj.params.LitecoinRegTestParams;
import org.libdohj.params.LitecoinTestNet3Params;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Base58;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;

import lombok.extern.slf4j.Slf4j;

import org.jetbrains.annotations.NotNull;

@Slf4j
public final class AltCoinAddressValidator extends InputValidator {

    private String currencyCode;

    ///////////////////////////////////////////////////////////////////////////////////////////
    // Public methods
    ///////////////////////////////////////////////////////////////////////////////////////////

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    @Override
    public ValidationResult validate(String input) {
        ValidationResult validationResult = super.validate(input);
        if (!validationResult.isValid || currencyCode == null) {
            return validationResult;
        } else {
            ValidationResult wrongChecksum = new ValidationResult(false,
                    Res.get("validation.altcoin.wrongChecksum"));
            ValidationResult regexTestFailed = new ValidationResult(false,
                    Res.get("validation.altcoin.wrongStructure", currencyCode));

            switch (currencyCode) {
                case "BTC":
                    try {
                        switch (BisqEnvironment.getBaseCurrencyNetwork()) {
                            case BTC_MAINNET:
                                Address.fromBase58(MainNetParams.get(), input);
                                break;
                            case BTC_TESTNET:
                                Address.fromBase58(TestNet3Params.get(), input);
                                break;
                            case BTC_REGTEST:
                                Address.fromBase58(RegTestParams.get(), input);
                                break;
                            case LTC_MAINNET:
                            case LTC_TESTNET:
                            case LTC_REGTEST:
                            case DASH_MAINNET:
                            case DASH_TESTNET:
                            case DASH_REGTEST:
                                // We cannot use MainNetParams because that would be one of the other base currencies,
                                // so we cloned the MainNetParams to BtcMainNetParamsForValidation
                                Address.fromBase58(BtcMainNetParamsForValidation.get(), input);
                                return new ValidationResult(true);
                        }
                        return new ValidationResult(true);
                    } catch (AddressFormatException e) {
                        return new ValidationResult(false, getErrorMessage(e));
                    }
                case "BSQ":
                    if (!input.startsWith("B"))
                        return new ValidationResult(false, Res.get("validation.altcoin.invalidAddress",
                                currencyCode, "BSQ address must start with \"B\""));

                    String addressAsBtc = input.substring(1, input.length());
                    try {
                        switch (BisqEnvironment.getBaseCurrencyNetwork()) {
                            case BTC_MAINNET:
                                Address.fromBase58(MainNetParams.get(), addressAsBtc);
                                break;
                            case BTC_TESTNET:
                                Address.fromBase58(TestNet3Params.get(), addressAsBtc);
                                break;
                            case BTC_REGTEST:
                                Address.fromBase58(RegTestParams.get(), addressAsBtc);
                                break;
                        }
                        return new ValidationResult(true);
                    } catch (AddressFormatException e) {
                        return new ValidationResult(false, getErrorMessage(e));
                    }
                case "LTC":
                    try {
                        switch (BisqEnvironment.getBaseCurrencyNetwork()) {
                            case BTC_MAINNET:
                            case BTC_TESTNET:
                            case BTC_REGTEST:
                            case DASH_MAINNET:
                            case DASH_TESTNET:
                            case DASH_REGTEST:
                            case LTC_MAINNET:
                                Address.fromBase58(LitecoinMainNetParams.get(), input);
                                break;
                            case LTC_TESTNET:
                                Address.fromBase58(LitecoinTestNet3Params.get(), input);
                                break;
                            case LTC_REGTEST:
                                Address.fromBase58(LitecoinRegTestParams.get(), input);
                                break;
                        }
                        return new InputValidator.ValidationResult(true);
                    } catch (AddressFormatException e) {
                        return new ValidationResult(false, getErrorMessage(e));
                    }
                case "DOGE":
                    try {
                        Address.fromBase58(DogecoinMainNetParams.get(), input);
                        return new ValidationResult(true);
                    } catch (AddressFormatException e) {
                        return new ValidationResult(false, getErrorMessage(e));
                    }
                case "DASH":
                    try {
                        switch (BisqEnvironment.getBaseCurrencyNetwork()) {
                            case BTC_MAINNET:
                            case BTC_TESTNET:
                            case BTC_REGTEST:
                            case LTC_MAINNET:
                            case LTC_TESTNET:
                            case LTC_REGTEST:
                            case DASH_MAINNET:
                                Address.fromBase58(DashMainNetParams.get(), input);
                                break;
                            case DASH_TESTNET:
                                Address.fromBase58(DashTestNet3Params.get(), input);
                                break;
                            case DASH_REGTEST:
                                Address.fromBase58(DashRegTestParams.get(), input);
                                break;
                        }
                        return new ValidationResult(true);
                    } catch (AddressFormatException e) {
                        return new ValidationResult(false, getErrorMessage(e));
                    }
                case "ETH":
                    // https://github.com/ethereum/web3.js/blob/master/lib/utils/utils.js#L403
                    if (!input.matches("^(0x)?[0-9a-fA-F]{40}$"))
                        return regexTestFailed;
                    else
                        return new ValidationResult(true);
                case "PHR":
                    if (input.matches("^[P][a-km-zA-HJ-NP-Z1-9]{25,34}$")) {
                        //noinspection ConstantConditions
                        try {
                            Address.fromBase58(PhoreParams.get(), input);
                            return new ValidationResult(true);
                        } catch (AddressFormatException e) {
                            return new ValidationResult(false, getErrorMessage(e));
                        }
                    } else {
                        return regexTestFailed;
                    }

                case "PIVX":
                    if (input.matches("^[D][a-km-zA-HJ-NP-Z1-9]{25,34}$")) {
                        //noinspection ConstantConditions
                        try {
                            Address.fromBase58(PivxParams.get(), input);
                            return new ValidationResult(true);
                        } catch (AddressFormatException e) {
                            return new ValidationResult(false, getErrorMessage(e));
                        }
                    } else {
                        return regexTestFailed;
                    }
                case "IOP":
                    if (input.matches("^[p][a-km-zA-HJ-NP-Z1-9]{25,34}$")) {
                        //noinspection ConstantConditions
                        try {
                            Address.fromBase58(IOPParams.get(), input);
                            return new ValidationResult(true);
                        } catch (AddressFormatException e) {
                            return new ValidationResult(false, getErrorMessage(e));
                        }
                    } else {
                        return regexTestFailed;
                    }
                case "888":
                    if (input.matches("^[83][a-km-zA-HJ-NP-Z1-9]{25,34}$")) {
                        if (OctocoinAddressValidator.ValidateAddress(input)) {
                            try {
                                Address.fromBase58(OctocoinParams.get(), input);
                                return new ValidationResult(true);
                            } catch (AddressFormatException e) {
                                return new ValidationResult(false, getErrorMessage(e));
                            }
                        } else {
                            return wrongChecksum;
                        }
                    } else {
                        return regexTestFailed;
                    }
                case "ZEC":
                    // We only support t addresses (transparent transactions)
                    if (input.startsWith("t"))
                        return validationResult;
                    else
                        return new ValidationResult(false, Res.get("validation.altcoin.zAddressesNotSupported"));
                case "GBYTE":
                    return ByteballAddressValidator.validate(input);
                case "NXT":
                    if (!input.startsWith("NXT-") || !input.equals(input.toUpperCase())) {
                        return regexTestFailed;
                    }
                    try {
                        long accountId = NxtReedSolomonValidator.decode(input.substring(4));
                        return new ValidationResult(accountId != 0);
                    } catch (NxtReedSolomonValidator.DecodeException e) {
                        return wrongChecksum;
                    }
                case "DCT":
                    if (input.matches("^(?=.{5,63}$)([a-z][a-z0-9-]+[a-z0-9])(\\.[a-z][a-z0-9-]+[a-z0-9])*$"))
                        return new ValidationResult(true);
                    else
                        return regexTestFailed;
                case "PNC":
                    if (input.matches("^[P3][a-km-zA-HJ-NP-Z1-9]{25,34}$")) {
                        if (PNCAddressValidator.ValidateAddress(input)) {
                            try {
                                Address.fromBase58(PNCParams.get(), input);
                                return new ValidationResult(true);
                            } catch (AddressFormatException e) {
                                return new ValidationResult(false, getErrorMessage(e));
                            }
                        } else {
                            return wrongChecksum;
                        }
                    } else {
                        return regexTestFailed;
                    }
                case "WAC":
                    try {
                        Address.fromBase58(WACoinsParams.get(), input);
                    } catch (AddressFormatException e) {
                        return new ValidationResult(false, getErrorMessage(e));
                    }
                    return new ValidationResult(true);
                case "ZEN":
                    try {
                        // Get the non Base58 form of the address and the bytecode of the first two bytes
                        byte[] byteAddress = Base58.decodeChecked(input);
                        int version0 = byteAddress[0] & 0xFF;
                        int version1 = byteAddress[1] & 0xFF;

                        // We only support public ("zn" (0x20,0x89), "t1" (0x1C,0xB8))
                        // and multisig ("zs" (0x20,0x96), "t3" (0x1C,0xBD)) addresses

                        // Fail for private addresses
                        if (version0 == 0x16 && version1 == 0x9A) {
                            // Address starts with "zc"
                            return new ValidationResult(false, Res.get("validation.altcoin.zAddressesNotSupported"));
                        } else if (version0 == 0x1C && (version1 == 0xB8 || version1 == 0xBD)) {
                            // "t1" or "t3" address
                            return new ValidationResult(true);
                        } else if (version0 == 0x20 && (version1 == 0x89 || version1 == 0x96)) {
                            // "zn" or "zs" address
                            return new ValidationResult(true);
                        } else {
                            // Unknown Type
                            return new ValidationResult(false);
                        }
                    } catch (AddressFormatException e) {
                        // Unhandled Exception (probably a checksum error)
                        return new ValidationResult(false);
                    }
                case "ELLA":
                    // https://github.com/ethereum/web3.js/blob/master/lib/utils/utils.js#L403
                    if (!input.matches("^(0x)?[0-9a-fA-F]{40}$"))
                        return regexTestFailed;
                    else
                        return new ValidationResult(true);
                case "XCN":
                    // https://bitcointalk.org/index.php?topic=1801595
                    return XCNAddressValidator.ValidateAddress(input);
                case "TRC":
                    try {
                        Address.fromBase58(TerracoinParams.get(), input);
                    } catch (AddressFormatException e) {
                        return new ValidationResult(false, getErrorMessage(e));
                    }
                    return new ValidationResult(true);
                case "INXT":
                    if (!input.matches("^(0x)?[0-9a-fA-F]{40}$"))
                        return regexTestFailed;
                    else
                        return new ValidationResult(true);
                case "PART":
                    if (input.matches("^[RP][a-km-zA-HJ-NP-Z1-9]{25,34}$")) {
                        //noinspection ConstantConditions
                        try {
                            Address.fromBase58(PARTParams.get(), input);
                            return new ValidationResult(true);
                        } catch (AddressFormatException e) {
                            return new ValidationResult(false, getErrorMessage(e));
                        }
                    } else {
                        return regexTestFailed;
                    }
                case "MDC":
                    if (input.matches("^m[a-zA-Z0-9]{26,33}$"))
                        return new ValidationResult(true);
                    else
                        return regexTestFailed;
                case "BCH":
                    try {
                        Address.fromBase58(BtcMainNetParamsForValidation.get(), input);
                        return new ValidationResult(true);
                    } catch (AddressFormatException e) {
                        return new ValidationResult(false, getErrorMessage(e));
                    }
                case "BCHC":
                    try {
                        Address.fromBase58(BtcMainNetParamsForValidation.get(), input);
                        return new ValidationResult(true);
                    } catch (AddressFormatException e) {
                        return new ValidationResult(false, getErrorMessage(e));
                    }
                case "BTG":
                    try {
                        Address.fromBase58(BTGParams.get(), input);
                        return new ValidationResult(true);
                    } catch (AddressFormatException e) {
                        return new ValidationResult(false, getErrorMessage(e));
                    }
                case "CAGE":
                    if (input.matches("^[D][a-zA-Z0-9]{26,34}$")) {
                        //noinspection ConstantConditions
                        try {
                            Address.fromBase58(CageParams.get(), input);
                            return new ValidationResult(true);
                        } catch (AddressFormatException e) {
                            return new ValidationResult(false, getErrorMessage(e));
                        }
                    } else {
                        return regexTestFailed;
                    }
                case "CRED":
                    if (!input.matches("^(0x)?[0-9a-fA-F]{40}$"))
                        return regexTestFailed;
                    else
                        return new ValidationResult(true);
                case "XSPEC":
                    try {
                        Address.fromBase58(XspecParams.get(), input);
                        return new ValidationResult(true);
                    } catch (AddressFormatException e) {
                        return new ValidationResult(false, getErrorMessage(e));
                    }
                case "WILD":
                    // https://github.com/ethereum/web3.js/blob/master/lib/utils/utils.js#L403
                    if (!input.matches("^(0x)?[0-9a-fA-F]{40}$"))
                        return regexTestFailed;
                    else
                        return new ValidationResult(true);
                case "ONION":
                    try {
                        Address.fromBase58(OnionParams.get(), input);
                        return new ValidationResult(true);
                    } catch (AddressFormatException e) {
                        return new ValidationResult(false, getErrorMessage(e));
                    }
                case "CREA":
                    try {
                        Address.fromBase58(CreaParams.get(), input);
                        return new ValidationResult(true);
                    } catch (AddressFormatException e) {
                        return new ValidationResult(false, getErrorMessage(e));
                    }
                case "XIN":
                    if (!input.matches("^XIN-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{5}$"))
                        return regexTestFailed;
                    else
                        return new ValidationResult(true);
                case "BETR":
                    // https://github.com/ethereum/web3.js/blob/master/lib/utils/utils.js#L403
                    if (!input.matches("^(0x)?[0-9a-fA-F]{40}$"))
                        return regexTestFailed;
                    else
                        return new ValidationResult(true);
                case "MVT":
                    if (!input.matches("^(0x)?[0-9a-fA-F]{40}$"))
                        return regexTestFailed;
                    else
                        return new ValidationResult(true);
                case "REF":
                    // https://github.com/ethereum/web3.js/blob/master/lib/utils/utils.js#L403
                    if (!input.matches("^(0x)?[0-9a-fA-F]{40}$"))
                        return regexTestFailed;
                    else
                        return new ValidationResult(true);
                case "STL":
                    if (!input.matches("^(Se)\\d[0-9A-Za-z]{94}$"))
                        return regexTestFailed;
                    else
                        return new ValidationResult(true);
                case "DAI":
                    // https://github.com/ethereum/web3.js/blob/master/lib/utils/utils.js#L403
                    if (!input.matches("^(0x)?[0-9a-fA-F]{40}$"))
                        return regexTestFailed;
                    else
                        return new ValidationResult(true);
                case "YTN":
                    return YTNAddressValidator.ValidateAddress(input);
                case "DARX":
                    if (!input.matches("^[R][a-km-zA-HJ-NP-Z1-9]{25,34}$"))
                        return regexTestFailed;
                    else
                        return new ValidationResult(true);
                case "ODN":
                    try {
                        Address.fromBase58(ODNParams.get(), input);
                        return new ValidationResult(true);
                    } catch (AddressFormatException e) {
                        return new ValidationResult(false, getErrorMessage(e));
                    }
                case "CDT":
                    if (input.startsWith("D"))
                        return new ValidationResult(true);
                    else
                        return new ValidationResult(false);
                case "DGM":
                    if (input.matches("^[D-E][a-zA-Z0-9]{33}$"))
                        return new ValidationResult(true);
                    else
                        return regexTestFailed;
                case "SCS":
                    try {
                        Address.fromBase58(SpeedCashParams.get(), input);
                        return new ValidationResult(true);
                    } catch (AddressFormatException e) {
                        return new ValidationResult(false, getErrorMessage(e));
                    }
                case "SOS":
                    if (!input.matches("^(0x)?[0-9a-fA-F]{40}$"))
                        return regexTestFailed;
                    else
                        return new ValidationResult(true);
                case "ACH":
                    try {
                        Address.fromBase58(ACHParams.get(), input);
                        return new ValidationResult(true);
                    } catch (AddressFormatException e) {
                        return new ValidationResult(false, getErrorMessage(e));
                    }
                case "VDN":
                    if (!input.matches("^[D][0-9a-zA-Z]{33}$"))
                        return regexTestFailed;
                    else
                        return new ValidationResult(true);
                case "ALC":
                    if (input.matches("^[A][a-km-zA-HJ-NP-Z1-9]{25,34}$")) {
                        //noinspection ConstantConditions
                        try {
                            Address.fromBase58(AlcParams.get(), input);
                            return new ValidationResult(true);
                        } catch (AddressFormatException e) {
                            return new ValidationResult(false, getErrorMessage(e));
                        }
                    } else {
                        return regexTestFailed;
                    }
                case "DIN":
                    if (!input.matches("^[D][0-9a-zA-Z]{33}$"))
                        return regexTestFailed;
                    else
                        return new ValidationResult(true);
                case "NAH":
                    if (input.matches("^[S][a-zA-Z0-9]{26,34}$")) {
                        //noinspection ConstantConditions
                        try {
                            Address.fromBase58(StrayaParams.get(), input);
                            return new ValidationResult(true);
                        } catch (AddressFormatException e) {
                            return new ValidationResult(false, getErrorMessage(e));
                        }
                    } else {
                        return regexTestFailed;
                    }
                case "ROI":
                    if (!input.matches("^[R][0-9a-zA-Z]{33}$"))
                        return regexTestFailed;
                    else
                        return new ValidationResult(true);
                case "WMCC":
                    return WMCCAddressValidator.ValidateAddress(WMCCParams.get(), input);
                case "RTO":
                    if (!input.matches("^[A][0-9A-Za-z]{94}$"))
                        return regexTestFailed;
                    else
                        return new ValidationResult(true);
                case "KOTO":
                    return KOTOAddressValidator.ValidateAddress(input);

                    // Add new coins at the end...
                default:
                    log.debug("Validation for AltCoinAddress not implemented yet. currencyCode: " + currencyCode);
                    return validationResult;
            }
        }
    }

    @NotNull
    private String getErrorMessage(AddressFormatException e) {
        return Res.get("validation.altcoin.invalidAddress", currencyCode, e.getMessage());
    }
}
