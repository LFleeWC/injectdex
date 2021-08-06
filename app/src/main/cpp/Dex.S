     .global blob
     .global blob_size
     .section .rodata
 blob:
 #if defined __i686__
     .incbin "C:\\Users\\20210301\\Desktop\\MyApplication\\app\\src\\main\\cpp\\mydex.dex"
 #elif defined __x86_64__
     .incbin "mydex-arm.dex"
 #elif defined __aarch64__
     .incbin "mydex-arm.dex"
 #elif defined __arm__
     .incbin "C:\\Users\\20210301\\Desktop\\MyApplication\\app\\src\\main\\cpp\\mydex.dex"
 #else
 # error Unsupported arch
 #endif
 1:
 blob_size:
     .int 1b - blob
