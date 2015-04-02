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

package io.bitsquare.trade.protocol.trade.seller.taker.tasks;

import io.bitsquare.common.taskrunner.TaskRunner;
import io.bitsquare.trade.TakerAsBuyerTrade;
import io.bitsquare.trade.TakerAsSellerTrade;
import io.bitsquare.trade.Trade;
import io.bitsquare.trade.protocol.trade.taker.tasks.TakerTradeTask;

import org.bitcoinj.core.Transaction;

import com.google.common.util.concurrent.FutureCallback;

import org.jetbrains.annotations.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TakerSignsAndPublishPayoutTx extends TakerTradeTask {
    private static final Logger log = LoggerFactory.getLogger(TakerSignsAndPublishPayoutTx.class);

    public TakerSignsAndPublishPayoutTx(TaskRunner taskHandler, Trade trade) {
        super(taskHandler, trade);
    }

    @Override
    protected void doRun() {
        try {
            processModel.getTradeWalletService().signAndPublishPayoutTx(
                    trade.getDepositTx(),
                    processModel.tradingPeer.getSignature(),
                    processModel.tradingPeer.getPayoutAmount(),
                    processModel.getPayoutAmount(),
                    processModel.tradingPeer.getPayoutAddressString(),
                    processModel.getAddressEntry(),
                    processModel.tradingPeer.getTradeWalletPubKey(),
                    processModel.getTradeWalletPubKey(),
                    processModel.getArbitratorPubKey(),
                    new FutureCallback<Transaction>() {
                        @Override
                        public void onSuccess(Transaction transaction) {
                            processModel.setPayoutTx(transaction);

                            if (trade instanceof TakerAsBuyerTrade)
                                trade.setProcessState(TakerAsBuyerTrade.ProcessState.PAYOUT_PUBLISHED);
                            else if (trade instanceof TakerAsSellerTrade)
                                trade.setProcessState(TakerAsSellerTrade.ProcessState.PAYOUT_PUBLISHED);

                            complete();
                        }

                        @Override
                        public void onFailure(@NotNull Throwable t) {
                            t.printStackTrace();
                            trade.setThrowable(t);
                            failed(t);
                        }
                    });
        } catch (Throwable t) {
            t.printStackTrace();
            trade.setThrowable(t);
            failed(t);
        }
    }
}