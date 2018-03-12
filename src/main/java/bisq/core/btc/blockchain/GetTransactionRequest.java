package bisq.core.btc.blockchain;

import bisq.core.btc.blockchain.providers.BlockchainTxProvider;

import bisq.common.Timer;
import bisq.common.UserThread;
import bisq.common.util.Utilities;

import org.bitcoinj.core.Coin;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.SettableFuture;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jetbrains.annotations.NotNull;

class GetTransactionRequest {
    private static final Logger log = LoggerFactory.getLogger(GetTransactionRequest.class);
    private static final ListeningExecutorService executorService = Utilities.getListeningExecutorService("GetFeeRequest", 3, 5, 10 * 60);
    private Timer timer;
    private int faults;

    public GetTransactionRequest() {
    }

    public SettableFuture<Coin> request(String transactionId, BlockchainTxProvider provider) {
        final SettableFuture<Coin> resultFuture = SettableFuture.create();
        return request(transactionId, provider, resultFuture);
    }

    private SettableFuture<Coin> request(String transactionId, BlockchainTxProvider provider, SettableFuture<Coin> resultFuture) {
        ListenableFuture<Coin> future = executorService.submit(() -> {
            Thread.currentThread().setName("requestFee-" + provider.toString());
            try {
                return provider.getFee(transactionId);
            } catch (IOException e) {
                log.debug("Fee request failed for tx {} from provider {}\n" +
                                "That is expected if the tx was not propagated yet to the provider.\n" +
                                "error={}",
                        transactionId, provider, e.getMessage());
                throw e;
            }
        });

        Futures.addCallback(future, new FutureCallback<Coin>() {
            public void onSuccess(Coin fee) {
                log.debug("Received fee of {}\nfor tx {}\nfrom provider {}", fee.toFriendlyString(), transactionId, provider);
                resultFuture.set(fee);
            }

            public void onFailure(@NotNull Throwable throwable) {
                if (timer == null) {
                    timer = UserThread.runAfter(() -> {
                        stopTimer();
                        faults++;
                        if (!resultFuture.isDone()) {
                            if (faults < 4) {
                                request(transactionId, provider, resultFuture);
                            } else {
                                resultFuture.setException(throwable);
                            }
                        } else {
                            log.debug("Got an error after a successful result. " +
                                    "That might happen when we get a delayed response from a timer request.");
                        }
                    }, 1 + faults);
                } else {
                    log.warn("Timer was not null");
                }
            }
        });

        return resultFuture;
    }

    private void stopTimer() {
        timer.stop();
        timer = null;
    }
}
