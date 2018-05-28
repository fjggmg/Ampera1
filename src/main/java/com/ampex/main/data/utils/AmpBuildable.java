package com.ampex.main.data.utils;

import amp.serialization.IAmpByteSerializable;

public interface AmpBuildable extends IAmpByteSerializable {
    void build(byte[] serialized) throws InvalidAmpBuildException;
}
