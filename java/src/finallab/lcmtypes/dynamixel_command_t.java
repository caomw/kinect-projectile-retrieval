/* LCM type definition class file
 * This file was automatically generated by lcm-gen
 * DO NOT MODIFY BY HAND!!!!
 */

package finallab.lcmtypes;
 
import java.io.*;
import java.util.*;
import lcm.lcm.*;
 
public final class dynamixel_command_t implements lcm.lcm.LCMEncodable
{
    public long utime;
    public double position_radians;
    public double speed;
    public double max_torque;
 
    public dynamixel_command_t()
    {
    }
 
    public static final long LCM_FINGERPRINT;
    public static final long LCM_FINGERPRINT_BASE = 0x94bff3111878405eL;
 
    static {
        LCM_FINGERPRINT = _hashRecursive(new ArrayList<Class<?>>());
    }
 
    public static long _hashRecursive(ArrayList<Class<?>> classes)
    {
        if (classes.contains(finallab.lcmtypes.dynamixel_command_t.class))
            return 0L;
 
        classes.add(finallab.lcmtypes.dynamixel_command_t.class);
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
 
        outs.writeDouble(this.position_radians); 
 
        outs.writeDouble(this.speed); 
 
        outs.writeDouble(this.max_torque); 
 
    }
 
    public dynamixel_command_t(byte[] data) throws IOException
    {
        this(new LCMDataInputStream(data));
    }
 
    public dynamixel_command_t(DataInput ins) throws IOException
    {
        if (ins.readLong() != LCM_FINGERPRINT)
            throw new IOException("LCM Decode error: bad fingerprint");
 
        _decodeRecursive(ins);
    }
 
    public static finallab.lcmtypes.dynamixel_command_t _decodeRecursiveFactory(DataInput ins) throws IOException
    {
        finallab.lcmtypes.dynamixel_command_t o = new finallab.lcmtypes.dynamixel_command_t();
        o._decodeRecursive(ins);
        return o;
    }
 
    public void _decodeRecursive(DataInput ins) throws IOException
    {
        this.utime = ins.readLong();
 
        this.position_radians = ins.readDouble();
 
        this.speed = ins.readDouble();
 
        this.max_torque = ins.readDouble();
 
    }
 
    public finallab.lcmtypes.dynamixel_command_t copy()
    {
        finallab.lcmtypes.dynamixel_command_t outobj = new finallab.lcmtypes.dynamixel_command_t();
        outobj.utime = this.utime;
 
        outobj.position_radians = this.position_radians;
 
        outobj.speed = this.speed;
 
        outobj.max_torque = this.max_torque;
 
        return outobj;
    }
 
}

