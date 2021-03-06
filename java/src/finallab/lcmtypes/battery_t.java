/* LCM type definition class file
 * This file was automatically generated by lcm-gen
 * DO NOT MODIFY BY HAND!!!!
 */

package finallab.lcmtypes;
 
import java.io.*;
import java.util.*;
import lcm.lcm.*;
 
public final class battery_t implements lcm.lcm.LCMEncodable
{
    public long utime;
    public float voltage;
 
    public battery_t()
    {
    }
 
    public static final long LCM_FINGERPRINT;
    public static final long LCM_FINGERPRINT_BASE = 0xca4fc738c55a436cL;
 
    static {
        LCM_FINGERPRINT = _hashRecursive(new ArrayList<Class<?>>());
    }
 
    public static long _hashRecursive(ArrayList<Class<?>> classes)
    {
        if (classes.contains(finallab.lcmtypes.battery_t.class))
            return 0L;
 
        classes.add(finallab.lcmtypes.battery_t.class);
        long hash = LCM_FINGERPRINT_BASE
            ;
        classes.remove(classes.size() - 1);
        return (hash<<1) + ((hash>>63)&1);
    }
 
    public void encode(DataOutput outs) throws IOException
    {
        outs.writeLong(LCM_FINGERPRINT);
        _encodeRecursive(outs);
    }
 
    public void _encodeRecursive(DataOutput outs) throws IOException
    {
        outs.writeLong(this.utime); 
 
        outs.writeFloat(this.voltage); 
 
    }
 
    public battery_t(byte[] data) throws IOException
    {
        this(new LCMDataInputStream(data));
    }
 
    public battery_t(DataInput ins) throws IOException
    {
        if (ins.readLong() != LCM_FINGERPRINT)
            throw new IOException("LCM Decode error: bad fingerprint");
 
        _decodeRecursive(ins);
    }
 
    public static finallab.lcmtypes.battery_t _decodeRecursiveFactory(DataInput ins) throws IOException
    {
        finallab.lcmtypes.battery_t o = new finallab.lcmtypes.battery_t();
        o._decodeRecursive(ins);
        return o;
    }
 
    public void _decodeRecursive(DataInput ins) throws IOException
    {
        this.utime = ins.readLong();
 
        this.voltage = ins.readFloat();
 
    }
 
    public finallab.lcmtypes.battery_t copy()
    {
        finallab.lcmtypes.battery_t outobj = new finallab.lcmtypes.battery_t();
        outobj.utime = this.utime;
 
        outobj.voltage = this.voltage;
 
        return outobj;
    }
 
}

