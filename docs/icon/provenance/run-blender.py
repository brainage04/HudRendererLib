"""Run finite Blender CLI scripts sequentially with two CPU threads in weight-20 cgroup."""
import json
import os
import subprocess
import sys
import time
from pathlib import Path
ROOT = Path(__file__).resolve().parent
CG = Path('/sys/fs/cgroup/user.slice/user-1000.slice/user@1000.service/app.slice/render-blender.service')

def enter_cgroup():
    (CG/'cgroup.procs').write_text(str(os.getpid()))
    os.nice(15)

def run(filename):
    assert (CG/'cpu.weight').read_text().strip() == '20'
    quota, period = (CG/'cpu.max').read_text().split()
    assert quota != 'max' and int(quota)/int(period) <= 3
    cpus = sorted(os.sched_getaffinity(0))[:2]
    env = {k:v for k,v in os.environ.items() if k not in ('DISPLAY','WAYLAND_DISPLAY')}
    env.update(CUDA_VISIBLE_DEVICES='', HIP_VISIBLE_DEVICES='', ROCR_VISIBLE_DEVICES='', OMP_NUM_THREADS='2', OPENBLAS_NUM_THREADS='2', SDL_AUDIODRIVER='dummy', PYTHONDONTWRITEBYTECODE='1')
    command = ['taskset','-c',','.join(map(str,cpus)),'nix','shell','nixpkgs#blender','--command','blender','--background','-noaudio','--threads','2','--python-exit-code','1','--python',str(ROOT/filename)]
    started=time.monotonic()
    with (ROOT/'evidence'/(Path(filename).stem+'.log')).open('w') as log:
        result=subprocess.run(command,env=env,cwd=ROOT,stdout=log,stderr=subprocess.STDOUT,preexec_fn=enter_cgroup)
    report={'script':filename,'command':command,'exit_code':result.returncode,'wall_seconds':time.monotonic()-started,'affinity_cpus':cpus,'render_threads':2,'cpu_weight':int((CG/'cpu.weight').read_text()),'cpu_max':(CG/'cpu.max').read_text().strip(),'display':None,'audio':None,'gpu':None}
    (ROOT/'evidence'/(Path(filename).stem+'-run.json')).write_text(json.dumps(report,indent=2)+'\n')
    print(json.dumps(report),flush=True)
    if result.returncode:
        print((ROOT/'evidence'/(Path(filename).stem+'.log')).read_text(),flush=True)
        raise SystemExit(result.returncode)

if __name__=='__main__':
    for filename in sys.argv[1:]:
        run(filename)
