using MatthiWare.CommandLine.Core.Attributes;

namespace BlueDispatcher.Options
{
    public class ProgramOptions
    {
        [Required, Name("m", "message"), Description("Message to send to server")]
        public string Message { get; set; }

        [Name("d", "device"), Description("Device to send to")]
        public string Device { get; set; }
    }
}
