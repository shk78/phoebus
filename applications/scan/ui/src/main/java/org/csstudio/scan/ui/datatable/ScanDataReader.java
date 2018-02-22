/*******************************************************************************
 * Copyright (c) 2018 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.scan.ui.datatable;

import static org.csstudio.scan.ScanSystem.logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;

import org.csstudio.scan.client.ScanClient;
import org.csstudio.scan.data.ScanData;
import org.phoebus.framework.jobs.NamedThreadFactory;

/** Periodically read data of a scan until the scan completes
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ScanDataReader
{
    public static interface Listener
    {
        /** Called when there's new logged data
         *  @param data Latest scan data
         */
        void dataChanged(ScanData data);

        /** Called when the scan completed, i.e. there won't be any new data */
        void scanCompleted();
    };

    private final ScanClient scan_client;
    private final long scan_id;
    private final Consumer<ScanData> data_listener;
    private final Runnable scan_complete_listener;
    private final ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Scan Data Table"));
    private ScheduledFuture<?> updates;

    /** Last known scan serial */
    private long last_serial = ScanClient.UNKNOWN_SCAN_SERIAL;

    public ScanDataReader(final ScanClient scan_client, final long scan_id, final Consumer<ScanData> data_listener,
                          final Runnable scan_complete_listener)
    {
        this.scan_id = scan_id;
        this.scan_client = scan_client;
        this.data_listener = data_listener;
        this.scan_complete_listener = scan_complete_listener;
        updates = timer.scheduleWithFixedDelay(this::poll, 100, 1000, TimeUnit.MILLISECONDS);
    }

    public long getScanId()
    {
        return scan_id;
    }

    private Void poll()
    {
        try
        {
            final long serial = scan_client.getLastScanDataSerial(scan_id);
            // Last_serial starts 'unknown'.
            if (serial > last_serial)
            {
                // As soon as the scan is known, even with 'no data' (serial -1),
                // fetch the data
                logger.log(Level.FINE, "Received data for scan {0}", scan_id);
                final ScanData data = scan_client.getScanData(scan_id);
                // Inform listener
                data_listener.accept(data);
                last_serial = serial;
            }
            // Is this a known scan, and it's done?
            if (serial >= 0  &&
                scan_client.getScanInfo(scan_id).getState().isDone())
            {
                updates.cancel(false);
                timer.shutdown();
                scan_complete_listener.run();
            }
            // else keep polling until the scan is 'done'.
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Scan data poll error for scan " + scan_id, ex);
        }
        return null;
    }
}
