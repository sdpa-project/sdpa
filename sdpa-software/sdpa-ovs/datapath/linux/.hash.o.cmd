cmd_/home/zzl/ovs/datapath/linux/hash.o := gcc -Wp,-MD,/home/zzl/ovs/datapath/linux/.hash.o.d  -nostdinc -isystem /usr/lib/gcc/i686-linux-gnu/4.8/include -I/home/zzl/ovs/include -I/home/zzl/ovs/datapath/linux/compat -I/home/zzl/ovs/datapath/linux/compat/include  -I/usr/src/linux-headers-3.11.0-12-generic/arch/x86/include -Iarch/x86/include/generated  -Iinclude -I/usr/src/linux-headers-3.11.0-12-generic/arch/x86/include/uapi -Iarch/x86/include/generated/uapi -I/usr/src/linux-headers-3.11.0-12-generic/include/uapi -Iinclude/generated/uapi -include /usr/src/linux-headers-3.11.0-12-generic/include/linux/kconfig.h -Iubuntu/include  -D__KERNEL__ -Wall -Wundef -Wstrict-prototypes -Wno-trigraphs -fno-strict-aliasing -fno-common -Werror-implicit-function-declaration -Wno-format-security -fno-delete-null-pointer-checks -O2 -m32 -msoft-float -mregparm=3 -freg-struct-return -fno-pic -mpreferred-stack-boundary=2 -march=i686 -mtune=generic -maccumulate-outgoing-args -Wa,-mtune=generic32 -ffreestanding -fstack-protector -DCONFIG_AS_CFI=1 -DCONFIG_AS_CFI_SIGNAL_FRAME=1 -DCONFIG_AS_CFI_SECTIONS=1 -DCONFIG_AS_AVX=1 -DCONFIG_AS_AVX2=1 -pipe -Wno-sign-compare -fno-asynchronous-unwind-tables -mno-sse -mno-mmx -mno-sse2 -mno-3dnow -mno-avx -Wframe-larger-than=1024 -Wno-unused-but-set-variable -fno-omit-frame-pointer -fno-optimize-sibling-calls -pg -Wdeclaration-after-statement -Wno-pointer-sign -fno-strict-overflow -fconserve-stack -DCC_HAVE_ASM_GOTO -DVERSION=\"2.1.2\" -I/home/zzl/ovs/datapath/linux/.. -I/home/zzl/ovs/datapath/linux/.. -g -include /home/zzl/ovs/datapath/linux/kcompat.h  -DMODULE  -D"KBUILD_STR(s)=\#s" -D"KBUILD_BASENAME=KBUILD_STR(hash)"  -D"KBUILD_MODNAME=KBUILD_STR(openvswitch)" -c -o /home/zzl/ovs/datapath/linux/.tmp_hash.o /home/zzl/ovs/datapath/linux/hash.c

source_/home/zzl/ovs/datapath/linux/hash.o := /home/zzl/ovs/datapath/linux/hash.c

deps_/home/zzl/ovs/datapath/linux/hash.o := \
  /home/zzl/ovs/datapath/linux/kcompat.h \
  include/generated/uapi/linux/version.h \
  include/linux/cache.h \
    $(wildcard include/config/smp.h) \
    $(wildcard include/config/arch/has/cache/line/size.h) \
  /home/zzl/ovs/datapath/linux/compat/include/linux/kernel.h \
  include/linux/kernel.h \
    $(wildcard include/config/lbdaf.h) \
    $(wildcard include/config/preempt/voluntary.h) \
    $(wildcard include/config/debug/atomic/sleep.h) \
    $(wildcard include/config/prove/locking.h) \
    $(wildcard include/config/ring/buffer.h) \
    $(wildcard include/config/tracing.h) \
    $(wildcard include/config/ftrace/mcount/record.h) \
  /usr/lib/gcc/i686-linux-gnu/4.8/include/stdarg.h \
  include/linux/linkage.h \
  /home/zzl/ovs/datapath/linux/compat/include/linux/compiler.h \
  include/linux/compiler.h \
    $(wildcard include/config/sparse/rcu/pointer.h) \
    $(wildcard include/config/trace/branch/profiling.h) \
    $(wildcard include/config/profile/all/branches.h) \
    $(wildcard include/config/enable/must/check.h) \
    $(wildcard include/config/enable/warn/deprecated.h) \
    $(wildcard include/config/kprobes.h) \
  /home/zzl/ovs/datapath/linux/compat/include/linux/compiler-gcc.h \
  include/linux/compiler-gcc.h \
    $(wildcard include/config/arch/supports/optimized/inlining.h) \
    $(wildcard include/config/optimize/inlining.h) \
  include/linux/compiler-gcc4.h \
    $(wildcard include/config/arch/use/builtin/bswap.h) \
  include/linux/stringify.h \
  include/linux/export.h \
    $(wildcard include/config/have/underscore/symbol/prefix.h) \
    $(wildcard include/config/modules.h) \
    $(wildcard include/config/modversions.h) \
    $(wildcard include/config/unused/symbols.h) \
  /usr/src/linux-headers-3.11.0-12-generic/arch/x86/include/asm/linkage.h \
    $(wildcard include/config/x86/32.h) \
    $(wildcard include/config/x86/64.h) \
    $(wildcard include/config/x86/alignment/16.h) \
  /home/zzl/ovs/datapath/linux/compat/include/linux/stddef.h \
  include/linux/stddef.h \
  include/uapi/linux/stddef.h \
  /home/zzl/ovs/include/linux/types.h \
  /home/zzl/ovs/datapath/linux/compat/include/linux/types.h \
  include/linux/types.h \
    $(wildcard include/config/uid16.h) \
    $(wildcard include/config/arch/dma/addr/t/64bit.h) \
    $(wildcard include/config/phys/addr/t/64bit.h) \
    $(wildcard include/config/64bit.h) \
  include/uapi/linux/types.h \
  /usr/src/linux-headers-3.11.0-12-generic/arch/x86/include/uapi/asm/types.h \
  /usr/src/linux-headers-3.11.0-12-generic/include/uapi/asm-generic/types.h \
  include/asm-generic/int-ll64.h \
  include/uapi/asm-generic/int-ll64.h \
  /usr/src/linux-headers-3.11.0-12-generic/arch/x86/include/uapi/asm/bitsperlong.h \
  include/asm-generic/bitsperlong.h \
  include/uapi/asm-generic/bitsperlong.h \
  /usr/src/linux-headers-3.11.0-12-generic/include/uapi/linux/posix_types.h \
  /usr/src/linux-headers-3.11.0-12-generic/arch/x86/include/asm/posix_types.h \
  /usr/src/linux-headers-3.11.0-12-generic/arch/x86/include/uapi/asm/posix_types_32.h \
  /usr/src/linux-headers-3.11.0-12-generic/include/uapi/asm-generic/posix_types.h \
  include/linux/bitops.h \
  /usr/src/linux-headers-3.11.0-12-generic/arch/x86/include/asm/bitops.h \
    $(wildcard include/config/x86/cmov.h) \
  /usr/src/linux-headers-3.11.0-12-generic/arch/x86/include/asm/alternative.h \
    $(wildcard include/config/paravirt.h) \
  /usr/src/linux-headers-3.11.0-12-generic/arch/x86/include/asm/asm.h \
  /usr/src/linux-headers-3.11.0-12-generic/arch/x86/include/asm/cpufeature.h \
    $(wildcard include/config/x86/debug/static/cpu/has.h) \
  /usr/src/linux-headers-3.11.0-12-generic/arch/x86/include/asm/required-features.h \
    $(wildcard include/config/x86/minimum/cpu/family.h) \
    $(wildcard include/config/math/emulation.h) \
    $(wildcard include/config/x86/pae.h) \
    $(wildcard include/config/x86/cmpxchg64.h) \
    $(wildcard include/config/x86/use/3dnow.h) \
    $(wildcard include/config/x86/p6/nop.h) \
    $(wildcard include/config/matom.h) \
  include/asm-generic/bitops/fls64.h \
  include/asm-generic/bitops/find.h \
    $(wildcard include/config/generic/find/first/bit.h) \
  include/asm-generic/bitops/sched.h \
  /usr/src/linux-headers-3.11.0-12-generic/arch/x86/include/asm/arch_hweight.h \
  include/asm-generic/bitops/const_hweight.h \
  include/asm-generic/bitops/le.h \
  /usr/src/linux-headers-3.11.0-12-generic/arch/x86/include/uapi/asm/byteorder.h \
  include/linux/byteorder/little_endian.h \
  include/uapi/linux/byteorder/little_endian.h \
  include/linux/swab.h \
  include/uapi/linux/swab.h \
  /usr/src/linux-headers-3.11.0-12-generic/arch/x86/include/uapi/asm/swab.h \
  include/linux/byteorder/generic.h \
  include/asm-generic/bitops/ext2-atomic-setbit.h \
  /home/zzl/ovs/datapath/linux/compat/include/linux/log2.h \
  include/linux/log2.h \
    $(wildcard include/config/arch/has/ilog2/u32.h) \
    $(wildcard include/config/arch/has/ilog2/u64.h) \
  include/linux/typecheck.h \
  include/linux/printk.h \
    $(wildcard include/config/early/printk.h) \
    $(wildcard include/config/printk.h) \
    $(wildcard include/config/dynamic/debug.h) \
  include/linux/init.h \
    $(wildcard include/config/broken/rodata.h) \
  include/linux/kern_levels.h \
  include/linux/dynamic_debug.h \
  include/uapi/linux/kernel.h \
  /usr/src/linux-headers-3.11.0-12-generic/include/uapi/linux/sysinfo.h \
  /usr/src/linux-headers-3.11.0-12-generic/arch/x86/include/asm/div64.h \
  /usr/src/linux-headers-3.11.0-12-generic/arch/x86/include/asm/cache.h \
    $(wildcard include/config/x86/l1/cache/shift.h) \
    $(wildcard include/config/x86/internode/cache/shift.h) \
    $(wildcard include/config/x86/vsmp.h) \
  include/linux/jhash.h \
  include/linux/unaligned/packed_struct.h \
  /home/zzl/ovs/datapath/linux/compat/include/linux/hash.h \
  include/linux/hash.h \
  /home/zzl/ovs/datapath/linux/compat/include/asm/hash.h \
    $(wildcard include/config/x86.h) \

/home/zzl/ovs/datapath/linux/hash.o: $(deps_/home/zzl/ovs/datapath/linux/hash.o)

$(deps_/home/zzl/ovs/datapath/linux/hash.o):
