/*
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */
/*
/*
 ******************************************************************************
 * Copyright (C) 2003, International Business Machines Corporation and   *
 * others. All Rights Reserved.                                               *
 ******************************************************************************
 *
 * Created on May 2, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
// CHANGELOG
//      2005-05-19 Edward Wang
//          - copy this file from icu4jsrc_3_2/src/com/ibm/icu/impl/StringPrepDataReader.java
//          - move from package com.ibm.icu.impl to package sun.net.idn
//
package sun.net.idn;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import sun.text.normalizer.ICUBinary;


/**
 * @author ram
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
final class StringPrepDataReader implements ICUBinary.Authenticate {

   /**
    * <p>private constructor.</p>
    * @param inputStream ICU uprop.dat file input stream
    * @exception IOException throw if data file fails authentication
    * @draft 2.1
    */
    public StringPrepDataReader(InputStream inputStream)
                                        throws IOException{

        unicodeVersion = ICUBinary.readHeader(inputStream, DATA_FORMAT_ID, this);


        dataInputStream = new DataInputStream(inputStream);

    }

    public void read(byte[] idnaBytes,
                        char[] mappingTable)
                        throws IOException{

        //Read the bytes that make up the idnaTrie
        dataInputStream.read(idnaBytes);

        //Read the extra data
        for(int i=0;i<mappingTable.length;i++){
            mappingTable[i]=dataInputStream.readChar();
        }
    }

    public byte[] getDataFormatVersion(){
        return DATA_FORMAT_VERSION;
    }

    public boolean isDataVersionAcceptable(byte version[]){
        return version[0] == DATA_FORMAT_VERSION[0]
               && version[2] == DATA_FORMAT_VERSION[2]
               && version[3] == DATA_FORMAT_VERSION[3];
    }
    public int[] readIndexes(int length)throws IOException{
        int[] indexes = new int[length];
        //Read the indexes
        for (int i = 0; i <length ; i++) {
             indexes[i] = dataInputStream.readInt();
        }
        return indexes;
    }

    public byte[] getUnicodeVersion(){
        return unicodeVersion;
    }
    // private data members -------------------------------------------------


    /**
    * ICU data file input stream
    */
    private DataInputStream dataInputStream;
    private byte[] unicodeVersion;
    /**
    * File format version that this class understands.
    * No guarantees are made if a older version is used
    * see store.c of gennorm for more information and values
    */
    ///* dataFormat="SPRP" 0x53, 0x50, 0x52, 0x50  */
    private static final byte DATA_FORMAT_ID[] = {(byte)0x53, (byte)0x50,
                                                    (byte)0x52, (byte)0x50};
    private static final byte DATA_FORMAT_VERSION[] = {(byte)0x3, (byte)0x2,
                                                        (byte)0x5, (byte)0x2};

}
