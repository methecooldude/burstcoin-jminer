/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 by luxe - https://github.com/de-luxe - BURST-LUXE-RED2-G6JW-H4HG5
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package burstcoin.jminer.core.reader.data;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * The type Plot drive.
 */
public class PlotDrive
{
  private static final Logger LOG = LoggerFactory.getLogger(PlotDrive.class);

  private Collection<PlotFile> plotFiles;
  private String directory;

  PlotDrive(String directory, Collection<Path> plotFilePaths, Long chunkPartNonces)
  {
    this.directory = directory;

    plotFiles = new HashSet<>();
	inProgress = '.plotting'
    for(Path path : plotFilePaths)
    {
	  if path.toLowerCase().contains(inProgress.toLowerCase()) {
		  continue;
	  }
      PlotFile plotFile = new PlotFile(path, chunkPartNonces);
      plotFiles.add(plotFile);

      if(plotFile.getStaggeramt() % plotFile.getNumberOfParts() != 0)
      {
        LOG.error("could not calculate valid numberOfParts: " + plotFile.getFilePath());
      }
    }
  }

  public Collection<PlotFile> getPlotFiles()
  {
    return plotFiles;
  }

  public String getDirectory()
  {
    return directory;
  }

  /* Collects chunk part start nonces.*/
  Map<BigInteger, Long> collectChunkPartStartNonces()
  {
    Map<BigInteger, Long> chunkPartStartNonces = new HashMap<>();
    for(PlotFile plotFile : plotFiles)
    {
      int expectedSize = chunkPartStartNonces.size() + plotFile.getChunkPartStartNonces().size();
      chunkPartStartNonces.putAll(plotFile.getChunkPartStartNonces());
      if(expectedSize != chunkPartStartNonces.size())
      {
        LOG.warn("possible overlapping plot-file '" + plotFile.getFilePath() + "', please check your plots.");
      }
    }
    return chunkPartStartNonces;
  }

  /* returns total number of bytes of all plotFiles */
  public long getSize()
  {
    long size = 0;
    for(PlotFile plotFile : plotFiles)
    {
      size += plotFile.getSize();
    }
    return size;
  }

  /* returns null if drive has mixed poc versions */
  public PocVersion getDrivePocVersion()
  {
    PocVersion drivePocVersion = null;
    for(PlotFile plotFile : plotFiles)
    {
      if(drivePocVersion == null)
      {
        drivePocVersion = plotFile.getPocVersion();
      }
      else if(!drivePocVersion.equals(plotFile.getPocVersion()))
      {
        return null;
      }
    }
    return drivePocVersion;
  }
}
