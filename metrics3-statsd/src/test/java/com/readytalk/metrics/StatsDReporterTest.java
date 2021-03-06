/**
 * Copyright (C) 2013 metrics-statsd contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.readytalk.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StatsDReporterTest {
  private final StatsD statsD = mock(StatsD.class);
  private final MetricRegistry registry = mock(MetricRegistry.class);
  private final StatsDReporter reporter = StatsDReporter.forRegistry(registry)
      .prefixedWith("prefix")
      .suffixedWith("suffix")
      .convertRatesTo(TimeUnit.SECONDS)
      .convertDurationsTo(TimeUnit.MILLISECONDS)
      .filter(MetricFilter.ALL)
      .build(statsD);

  @SuppressWarnings("rawtypes") //Metrics library specifies the raw Gauge type unfortunately
  private final SortedMap<String, Gauge> emptyGaugeMap = new TreeMap<String, Gauge>();

  @Test
  public void doesNotReportStringGaugeValues() throws Exception {
    reporter.report(map("%s.gauge", gauge("value")), this.<Counter>map(), this.<Histogram>map(), this.<Meter>map(),
            this.<Timer>map());

    final InOrder inOrder = inOrder(statsD);
    inOrder.verify(statsD).connect();
    inOrder.verify(statsD, never()).send("prefix.suffix.gauge", "value");
    inOrder.verify(statsD).close();
  }

  @Test
  public void reportsByteGaugeValues() throws Exception {
    reporter.report(map("%s.gauge", gauge((byte) 1)), this.<Counter>map(), this.<Histogram>map(), this.<Meter>map(),
        this.<Timer>map());

    final InOrder inOrder = inOrder(statsD);
    inOrder.verify(statsD).connect();
    inOrder.verify(statsD).send("prefix.suffix.gauge", "1");
    inOrder.verify(statsD).close();
  }

  @Test
  public void reportsShortGaugeValues() throws Exception {
    reporter.report(map("%s.gauge", gauge((short) 1)), this.<Counter>map(), this.<Histogram>map(), this.<Meter>map(),
        this.<Timer>map());

    final InOrder inOrder = inOrder(statsD);
    inOrder.verify(statsD).connect();
    inOrder.verify(statsD).send("prefix.suffix.gauge", "1");
    inOrder.verify(statsD).close();
  }

  @Test
  public void reportsIntegerGaugeValues() throws Exception {
    reporter.report(map("%s.gauge", gauge(1)), this.<Counter>map(), this.<Histogram>map(), this.<Meter>map(),
        this.<Timer>map());

    final InOrder inOrder = inOrder(statsD);
    inOrder.verify(statsD).connect();
    inOrder.verify(statsD).send("prefix.suffix.gauge", "1");
    inOrder.verify(statsD).close();
  }

  @Test
  public void reportsLongGaugeValues() throws Exception {
    reporter.report(map("%s.gauge", gauge(1L)), this.<Counter>map(), this.<Histogram>map(), this.<Meter>map(),
        this.<Timer>map());

    final InOrder inOrder = inOrder(statsD);
    inOrder.verify(statsD).connect();
    inOrder.verify(statsD).send("prefix.suffix.gauge", "1");
    inOrder.verify(statsD).close();
  }

  @Test
  public void reportsFloatGaugeValues() throws Exception {
    reporter.report(map("%s.gauge", gauge(1.1f)), this.<Counter>map(), this.<Histogram>map(), this.<Meter>map(),
        this.<Timer>map());

    final InOrder inOrder = inOrder(statsD);
    inOrder.verify(statsD).connect();
    inOrder.verify(statsD).send("prefix.suffix.gauge", "1.10");
    inOrder.verify(statsD).close();
  }

  @Test
  public void reportsDoubleGaugeValues() throws Exception {
    reporter.report(map("%s.gauge", gauge(1.1)), this.<Counter>map(), this.<Histogram>map(), this.<Meter>map(),
        this.<Timer>map());

    final InOrder inOrder = inOrder(statsD);
    inOrder.verify(statsD).connect();
    inOrder.verify(statsD).send("prefix.suffix.gauge", "1.10");
    inOrder.verify(statsD).close();
  }

  @Test
  public void reportsCounters() throws Exception {
    final Counter counter = mock(Counter.class);
    when(counter.getCount()).thenReturn(100L);

    reporter.report(emptyGaugeMap, this.<Counter>map("%s.counter", counter), this.<Histogram>map(),
        this.<Meter>map(), this.<Timer>map());

    final InOrder inOrder = inOrder(statsD);
    inOrder.verify(statsD).connect();
    inOrder.verify(statsD).send("prefix.suffix.counter", "100");
    inOrder.verify(statsD).close();
  }

  @Test
  public void reportsHistograms() throws Exception {
    final Histogram histogram = mock(Histogram.class);
    when(histogram.getCount()).thenReturn(1L);

    final Snapshot snapshot = mock(Snapshot.class);
    when(snapshot.getMax()).thenReturn(2L);
    when(snapshot.getMean()).thenReturn(3.0);
    when(snapshot.getMin()).thenReturn(4L);
    when(snapshot.getStdDev()).thenReturn(5.0);
    when(snapshot.getMedian()).thenReturn(6.0);
    when(snapshot.get75thPercentile()).thenReturn(7.0);
    when(snapshot.get95thPercentile()).thenReturn(8.0);
    when(snapshot.get98thPercentile()).thenReturn(9.0);
    when(snapshot.get99thPercentile()).thenReturn(10.0);
    when(snapshot.get999thPercentile()).thenReturn(11.0);

    when(histogram.getSnapshot()).thenReturn(snapshot);

    reporter.report(emptyGaugeMap, this.<Counter>map(), this.<Histogram>map("histogram.%s", histogram),
        this.<Meter>map(), this.<Timer>map());

    final InOrder inOrder = inOrder(statsD);

    inOrder.verify(statsD).connect();
    verify(statsD).send("prefix.histogram.suffix.samples", "1");
    verify(statsD).send("prefix.histogram.suffix.max", "2");
    verify(statsD).send("prefix.histogram.suffix.mean", "3.00");
    verify(statsD).send("prefix.histogram.suffix.min", "4");
    verify(statsD).send("prefix.histogram.suffix.stddev", "5.00");
    verify(statsD).send("prefix.histogram.suffix.p50", "6.00");
    verify(statsD).send("prefix.histogram.suffix.p75", "7.00");
    verify(statsD).send("prefix.histogram.suffix.p95", "8.00");
    verify(statsD).send("prefix.histogram.suffix.p98", "9.00");
    verify(statsD).send("prefix.histogram.suffix.p99", "10.00");
    inOrder.verify(statsD).send("prefix.histogram.suffix.p999", "11.00");

    inOrder.verify(statsD).close();
  }

  @Test
  public void reportsMeters() throws Exception {
    final Meter meter = mock(Meter.class);
    when(meter.getCount()).thenReturn(1L);
    when(meter.getOneMinuteRate()).thenReturn(2.0);
    when(meter.getFiveMinuteRate()).thenReturn(3.0);
    when(meter.getFifteenMinuteRate()).thenReturn(4.0);
    when(meter.getMeanRate()).thenReturn(5.0);

    reporter.report(emptyGaugeMap, this.<Counter>map(), this.<Histogram>map(), this.<Meter>map("meter.%s", meter),
        this.<Timer>map());

    final InOrder inOrder = inOrder(statsD);
    inOrder.verify(statsD).connect();
    verify(statsD).send("prefix.meter.suffix.samples", "1");
    verify(statsD).send("prefix.meter.suffix.m1_rate", "2.00");
    verify(statsD).send("prefix.meter.suffix.m5_rate", "3.00");
    verify(statsD).send("prefix.meter.suffix.m15_rate", "4.00");
    inOrder.verify(statsD).send("prefix.meter.suffix.mean_rate", "5.00");
    inOrder.verify(statsD).close();


  }

  @Test
  public void reportsTimers() throws Exception {
    final Timer timer = mock(Timer.class);
    when(timer.getCount()).thenReturn(1L);
    when(timer.getMeanRate()).thenReturn(2.0);
    when(timer.getOneMinuteRate()).thenReturn(3.0);
    when(timer.getFiveMinuteRate()).thenReturn(4.0);
    when(timer.getFifteenMinuteRate()).thenReturn(5.0);

    final Snapshot snapshot = mock(Snapshot.class);
    when(snapshot.getMax()).thenReturn(TimeUnit.MILLISECONDS.toNanos(100));
    when(snapshot.getMean()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(200));
    when(snapshot.getMin()).thenReturn(TimeUnit.MILLISECONDS.toNanos(300));
    when(snapshot.getStdDev()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(400));
    when(snapshot.getMedian()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(500));
    when(snapshot.get75thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(600));
    when(snapshot.get95thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(700));
    when(snapshot.get98thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(800));
    when(snapshot.get99thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(900));
    when(snapshot.get999thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(1000));

    when(timer.getSnapshot()).thenReturn(snapshot);

    reporter.report(emptyGaugeMap, this.<Counter>map(), this.<Histogram>map(), this.<Meter>map(),
        map("timer.%s", timer));

    final InOrder inOrder = inOrder(statsD);
    inOrder.verify(statsD).connect();
    verify(statsD).send("prefix.timer.suffix.max", "100.00");
    verify(statsD).send("prefix.timer.suffix.mean", "200.00");
    verify(statsD).send("prefix.timer.suffix.min", "300.00");
    verify(statsD).send("prefix.timer.suffix.stddev", "400.00");
    verify(statsD).send("prefix.timer.suffix.p50", "500.00");
    verify(statsD).send("prefix.timer.suffix.p75", "600.00");
    verify(statsD).send("prefix.timer.suffix.p95", "700.00");
    verify(statsD).send("prefix.timer.suffix.p98", "800.00");
    verify(statsD).send("prefix.timer.suffix.p99", "900.00");
    verify(statsD).send("prefix.timer.suffix.p999", "1000.00");
    verify(statsD).send("prefix.timer.suffix.samples", "1");
    verify(statsD).send("prefix.timer.suffix.m1_rate", "3.00");
    verify(statsD).send("prefix.timer.suffix.m5_rate", "4.00");
    verify(statsD).send("prefix.timer.suffix.m15_rate", "5.00");
    inOrder.verify(statsD).send("prefix.timer.suffix.mean_rate", "2.00");
    inOrder.verify(statsD).close();
  }

  private <T> SortedMap<String, T> map() {
    return new TreeMap<String, T>();
  }

  private <T> SortedMap<String, T> map(String name, T metric) {
    final TreeMap<String, T> map = new TreeMap<String, T>();
    map.put(name, metric);
    return map;
  }

  @SuppressWarnings("rawtypes")
  private <T> Gauge gauge(T value) {
    final Gauge gauge = mock(Gauge.class);
    when(gauge.getValue()).thenReturn(value);
    return gauge;
  }
}
