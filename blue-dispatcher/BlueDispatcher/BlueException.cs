using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace BlueDispatcher
{
    public class PigeonNotFoundException : Exception
    {
        public PigeonNotFoundException() : base("Blue Pigeon device could not be found")
        {
        }
    }
}
