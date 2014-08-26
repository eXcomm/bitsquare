/*
 * This file is part of Bitsquare.
 *
 * Bitsquare is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bitsquare is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bitsquare. If not, see <http://www.gnu.org/licenses/>.
 */

package io.bitsquare.trade.protocol.createoffer.tasks;

import io.bitsquare.btc.FeePolicy;
import io.bitsquare.btc.Restritions;
import io.bitsquare.trade.Offer;
import io.bitsquare.trade.handlers.FaultHandler;
import io.bitsquare.trade.handlers.ResultHandler;

import com.google.bitcoin.core.Coin;

import javax.annotation.concurrent.Immutable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.*;

@Immutable
public class ValidateOffer {
    private static final Logger log = LoggerFactory.getLogger(ValidateOffer.class);

    public static void run(ResultHandler resultHandler, FaultHandler faultHandler, Offer offer) {
        try {
            checkNotNull(offer.getAcceptedCountries());
            checkNotNull(offer.getAcceptedLanguageLocales());
            checkNotNull(offer.getAmount());
            checkNotNull(offer.getArbitrator());
            checkNotNull(offer.getBankAccountCountry());
            checkNotNull(offer.getBankAccountId());
            checkNotNull(offer.getCollateral());
            checkNotNull(offer.getCreationDate());
            checkNotNull(offer.getCurrency());
            checkNotNull(offer.getDirection());
            checkNotNull(offer.getId());
            checkNotNull(offer.getMessagePublicKey());
            checkNotNull(offer.getMinAmount());
            checkNotNull(offer.getPrice());

            checkArgument(offer.getAcceptedCountries().size() > 0);
            checkArgument(offer.getAcceptedLanguageLocales().size() > 0);
            checkArgument(offer.getMinAmount().compareTo(Restritions.MIN_TRADE_AMOUNT) >= 0);
            checkArgument(offer.getAmount().compareTo(Restritions.MIN_TRADE_AMOUNT) >= 0);
            checkArgument(offer.getAmount().compareTo(offer.getMinAmount()) >= 0);
            checkArgument(offer.getCollateral() > 0);
            checkArgument(offer.getPrice() > 0);

            // TODO check balance
            Coin collateralAsCoin = offer.getAmount().divide((long) (1d / offer.getCollateral()));
            Coin totalsToFund = collateralAsCoin.add(FeePolicy.CREATE_OFFER_FEE.add(FeePolicy.TX_FEE));
            // getAddressInfoByTradeID(offerId)
            // TODO when offer is flattened continue here...

            resultHandler.onResult();
        } catch (Throwable t) {
            faultHandler.onFault("Offer validation failed.", t);
        }
    }
}